package com.revenera.gcs;

import com.flexnet.external.webservice.keygenerator.LicenseGeneratorServiceInterface;
import com.revenera.gcs.implementor.AbstractImplementor;
import com.revenera.gcs.implementor.ImplementorFactory;
import com.revenera.gcs.utils.AnnotationManager;
import com.revenera.gcs.utils.Diagnostics;
import com.revenera.gcs.utils.GeneratorImplementor;
import com.revenera.gcs.utils.log.Level;
import com.revenera.gcs.utils.log.LoggingFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The root of the service, registered as a listener will set stuff up when the context is initialized
 */
@WebListener
public class Application implements ServletContextListener {
  /** logger */
  private static final LoggingFactory logger = LoggingFactory.create(Application.class);
  /** instance */
  private static final AtomicReference<Application> singleton = new AtomicReference<>();

  private static final ApplicationProiperties applicationProperties = new ApplicationProiperties();

  public static Application getInstance() {
    return singleton.get();
  }

  public static ApplicationProiperties getApplicationProperties() {
    return applicationProperties;
  }

  /** implementor factory */
  private final ImplementorFactory implementorFactory = new ImplementorFactory();
  /** diagnostics */
  private final Diagnostics diagnostics = new Diagnostics();

  /** ??? */
  private  String web_inf;

  static {
    try {

      LoggingFactory.setLoggingLevel(
          Level.valueOf(
              applicationProperties.getLoggingLevel().toUpperCase()));

      LoggingFactory.setLoggingRoot(applicationProperties.getLoggingRoot());

      logger.in();
    }
    catch (final Throwable t) {
      logger.exception(t);
    }
    finally {
      logger.out();
    }
  }

  /**
   * CTOR
   */
  public Application() {
    logger.in();
    try {
      singleton.getAndSet(this);

      logger.me(this);

      logger.info().log("version", applicationProperties.getVersionDetails());
    }
    catch (final Throwable t) {
      logger.exception(t);
    }
    finally {
      logger.out();
    }
  }

  /**
   * GETTERS
   */
  public final ImplementorFactory getImplementorFactory() {
    return implementorFactory;
  }

  public Diagnostics getDiagnostics() {
    return diagnostics;
  }

  private void logServletAttributes(final ServletContextEvent event) {
    logger.in();
    try {
      final ServletContext context = event.getServletContext();

      final Map<String, String> attributes = new LinkedHashMap<>();

      final Enumeration<String> itt = context.getAttributeNames();
      while (itt.hasMoreElements()) {
        final String name = itt.nextElement();
        final Object value = context.getAttribute(name);

        attributes.put(name, value != null ? value.toString() : "");
      }

      logger.yaml(Level.INFO, attributes);
    }
    catch (final Throwable t) {
      logger.exception(t);
    }
    finally {
      logger.out();
    }
  }

  public Path getResourcePath(final String...parts) {
    return Paths.get(this.web_inf, parts);
  }

  /**
   *
   */
  private ScheduledExecutorService scheduler;

  private void polling() {
    try {

      while (LoggingFactory.serializationPending()) {
        //
        LoggingFactory.serialize();
      }
    }
    catch (final Throwable t) {
      logger.exception(t);
    }
  }

  private void housekeeping() {
    try {
      logger.yaml(Level.DEBUG, this.diagnostics.serialize());
    }
    catch (final Throwable t) {
      logger.exception(t);
    }
  }

  private void startup(final int delay,final int period) {
    // TODO SCHEDULE
    scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread t = new Thread(r, "myapp-housekeeping");
      t.setDaemon(false); // can be true, but don't rely on daemon for cleanup
      t.setContextClassLoader(Application.class.getClassLoader());
      return t;
    });

    scheduler.scheduleAtFixedRate(this::housekeeping, delay, period, TimeUnit.MINUTES);

    scheduler.scheduleAtFixedRate(this::polling, 1, 1, TimeUnit.SECONDS);

    // TODO SCHEDULE
  }

  private void shutdown() {
    logger.in();
    if (scheduler != null) {
      scheduler.shutdown();                 // stop accepting new tasks
      try {
        // try 3 times
        for (int i = 0; i < 3; i++) {
          logger.verbose().log("scheduler await termination");

          if (scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
            logger.verbose().log("scheduler terminated");
            break;
          }
          scheduler.shutdownNow();
        }
      }
      catch (final InterruptedException ie) {
        logger.exception(ie);
        scheduler.shutdownNow();
        Thread.currentThread().interrupt();
      }
    }
  }

  @Override
  public void contextInitialized(final ServletContextEvent event) {
    logger.in();

    try {
//      logServletAttributes(event);

      startup(0, applicationProperties.getHousekeepingFrequency());

      this.web_inf = event.getServletContext().getRealPath("/WEB-INF");

      logger.info().log("resources", getResourcePath());

      final AnnotationManager manager = new AnnotationManager();

      final List<String> files = manager.findClassFilesInPackage(AbstractImplementor.class);

      for (final String typename : files) {

        final Class<?> type = Class.forName(typename);

        if (type.isAnnotationPresent(GeneratorImplementor.class)) {

          final GeneratorImplementor ann = type.getAnnotation(GeneratorImplementor.class);

          logger.info().log("found annotation",
              ann.technologyId(),
              ann.technologyName(),
              ann.isDefault(),
              type.getName());

          if (AbstractImplementor.class.isAssignableFrom(type)) {

            final AbstractImplementor imp = (AbstractImplementor) type.newInstance();

            imp.configureTechnologyProperties(ann.technologyId(), ann.technologyName());

            implementorFactory.addImplementor(imp, ann.isDefault());
          }
        }
      }

      final LicenseGeneratorServiceInterface implementor = implementorFactory.getDefaultImplementor();
      if (implementor != null) {
        logger.info().log("default implementor", implementor.getClass().getName());
      }
      else {
        throw new RuntimeException("No default implementor found");
      }
    }
    catch (final Throwable t) {
      logger.exception(t);
    }
    finally {
      logger.yaml(Level.DEBUG, ApplicationData.create());
    }
  }

  @Override
  public void contextDestroyed(final ServletContextEvent event) {
    logger.in();
    try {
      shutdown();
    }
    finally {
      logger.out();
    }
  }
}
