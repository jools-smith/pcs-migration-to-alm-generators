package com.revenera.gcs.transaction;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.revenera.gcs.utils.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Transaction extends NamedEntity {
  private final static LinkedBlockingQueue<Transaction> transactions = new LinkedBlockingQueue<>();

  public static final Log logger = Log.create(Transaction.class);

  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public class Element extends TimedEntity {
    Object request;
    Object response;

    /**
     *
     */
    private Element() {
      logger.me(this);
    }

    /**
     *
     * @param request
     * @return
     */
    @JsonIgnore
    public Element addRequest(final Object request) {
      // debug
      TransactionException.assertNotNull(request);
      TransactionException.assertNull(this.request);
      // debug
      this.request = request;
      return this;
    }

    /**
     *
     * @param response
     * @return
     */
    @JsonIgnore
    public Element addResponse(final Object response) {
      // debug
      TransactionException.assertNotNull(response);
      TransactionException.assertNull(this.response);
      // debug
      this.response = response;
      super.stop();
      return this;
    }
  }

  private final LinkedList<Element> elements = new LinkedList<>();
  private Object response;

  /**
   *
   * @param name
   */
  private Transaction(final String name) {
    super(name);
    logger.me(this);
  }

  /**
   *
   * @param response
   * @return
   */
  @JsonIgnore
  public Transaction addResponse(final Object response) {
    // debug
    TransactionException.assertNotNull(response);
    TransactionException.assertNull(this.response);
    // debug
    super.stop();
    this.response = response;
    return this;
  }

  /**
   *
   * @return
   */
  @JsonIgnore
  public Element lastElement() {
    return this.elements.getLast();
  }

  /**
   *
   * @return
   */
  @JsonIgnore
  public Transaction commit() {
    transactions.add(this);
    logger.array(Log.Level.info, "transaction size", transactions.size());
    return this;
  }

  /**
   *
   * @return
   */
  @JsonIgnore
  public Element createElement() {
    final Element element = new Element();
    this.elements.add(element);
    return element;
  }

  /**
   *
   * @return
   */
  public static List<Transaction> removeAll() {
    if (!transactions.isEmpty()) {
      final List<Transaction> list = new ArrayList<>();
      transactions.drainTo(list);
      return list;
    }
    return null;
  }

  /**
   *
   * @param name
   * @return
   */
  public static Transaction create(final String name) {
    return new Transaction(name);
  }
}
