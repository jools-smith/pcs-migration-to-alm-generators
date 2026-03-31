package com.revenera.gcs.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.IntStream;

import com.fasterxml.jackson.annotation.JsonIgnore;

public final class Diagnostics {

  public final static Log logger = Log.create(Diagnostics.class);

  private static final Function<Duration, String> reformat = (d) -> {
    final AtomicReference<String> str = new AtomicReference<>(d.toString().replace("PT","").replace("H",":").replace("M",":").replace("S",""));
    
    IntStream.range(0, 9).forEach(index -> {
      str.getAndUpdate((y) -> y.replace(":"+index+":", ":0"+index+":").replace(":"+index+".", ":0"+index+"."));
    });
    
    return str.get();
  };
  
  private static final Function<Instant, Duration> elapseNow = (i) -> Duration.between(i,  Instant.now());
  
  public final class Token {
    private final Class<?> clazz;
    private final String endpoint;
    private final Instant timestamp = Instant.now();

    private Token(final Class<?> clazz, final String endpoint) {
      this.clazz = clazz;
      this.endpoint = endpoint;
    }
    
    public void commit() {
      Diagnostics.this.touch(this);
    }
  }
  
  class Details {
    class Element {
      private long count = 0;
      private Instant accessed = null;
      private Duration elapse = Duration.ofNanos(0);
      
      @JsonIgnore
      void touch(final Token token) {
        this.count += 1;
        this.accessed = token.timestamp;
        
        this.elapse = this.elapse.plus(elapseNow.apply(token.timestamp));
      }
      
      public Long getCount() {
        return this.count;
      }

      public String getIdle() {
        return reformat.apply(elapseNow.apply(this.accessed));
      }
      public String getElapsed() {
        return reformat.apply(this.elapse);
      }
    }
    
    private final Map<String, Element> accessCounts = new TreeMap<>();
    private Instant lastAccessed = Instant.MIN;
    
    public Details() {
    }
    
    public void touch(final Token token) {
      
      if (!this.accessCounts.containsKey(token.endpoint)) {
        this.accessCounts.put(token.endpoint, new Element());
      }
      
      this.accessCounts.get(token.endpoint).touch(token);
      
      this.lastAccessed = token.timestamp;
    }
  }
 
  private final Instant started = Instant.now();
  private final Map<Class<?>, Details> classes = new HashMap<>();
  
  private Details forClass(final Class<?> clazz) {
    if (!this.classes.containsKey(clazz)) {
      this.classes.put(clazz, new Details());
    }
    return this.classes.get(clazz);
  }

  public Diagnostics() {
    logger.me(this);
  }

  @JsonIgnore
  synchronized private void touch(final Token token) {
    forClass(token.clazz).touch(token);
  }

  synchronized public Object serialize() {
    
    final Map<String, Object> obj = new LinkedHashMap<String,Object>();
    obj.put("up-at", started.toString());
    obj.put("up-for",  reformat.apply(elapseNow.apply(this.started)));
    
    for (final Entry<Class<?>, Details> entry : this.classes.entrySet()) {

      obj.put(entry.getKey().getSimpleName(), new LinkedHashMap<String,Object>() {
        {
          this.put("idle-for",  reformat.apply(elapseNow.apply(entry.getValue().lastAccessed)));
          this.put("accessCounts", entry.getValue().accessCounts);
        }
      });
    }

    return obj;
  }
  
  public Token getToken(final Class<?> clazz, final String endpoint) {
    return new Token(clazz, endpoint);
  }
}