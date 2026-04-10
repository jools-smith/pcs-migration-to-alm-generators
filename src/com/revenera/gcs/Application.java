package com.revenera.gcs;

import com.flexnet.external.webservice.keygenerator.LicenseGeneratorServiceInterface;
import com.revenera.gcs.implementor.AbstractImplementor;
import com.revenera.gcs.implementor.ImplementorFactory;
import com.revenera.gcs.implementor.TechnologyProperties;
import com.revenera.gcs.utils.AnnotationManager;
import com.revenera.gcs.utils.Diagnostics;
import com.revenera.gcs.utils.GeneratorImplementor;
import com.revenera.gcs.utils.Log;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
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
  private static final Log logger = Log.create(Application.class);
  /** instance */
  private static final AtomicReference<Application> singleton = new AtomicReference<>();

  private static final BuildVersion buildVersion = new BuildVersion();

  @SuppressWarnings("unused")
  public static Application singleton() {
    return singleton.get();
  }

  public static Application getInstance() {
    return singleton.get();
  }

  public static BuildVersion getBuildVersion() {
    return buildVersion;
  }

  /** implementor factory */
  private final ImplementorFactory implementorFactory = new ImplementorFactory();
  /** diagnostics */
  private final Diagnostics diagnostics = new Diagnostics();

  /** ??? */
  private  String web_inf;

  static {
    try {
      //TODO: we can reduce this potentially -- once levels have been assessed
      Log.setLoggingLevel(Log.Level.trace);
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

      logger.array(Log.Level.info, "version", buildVersion.getVersionDetails());
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

  private void logAttributeNames(final ServletContextEvent event) {
    logger.in();
    try {
      final Enumeration<String> itt = event.getServletContext().getAttributeNames();
      while (itt.hasMoreElements()) {
        logger.log(Log.Level.trace, itt.nextElement());
      }
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

  private void housekeeping() {
    logger.in();
    try {
      logger.json(Log.Level.debug, this.diagnostics.serialize());
    }
    catch (final Throwable t) {
      logger.exception(t);
    }
  }

  private void startup() {
    // TODO SCHEDULE
    scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread t = new Thread(r, "myapp-housekeeping");
      t.setDaemon(false); // can be true, but don't rely on daemon for cleanup
      t.setContextClassLoader(Application.class.getClassLoader());
      return t;
    });

    scheduler.scheduleAtFixedRate(this::housekeeping, 0, 1, TimeUnit.MINUTES);
    // TODO SCHEDULE
  }

  private void shutdown() {
    logger.in();
    if (scheduler != null) {
      scheduler.shutdown();                 // stop accepting new tasks
      try {
        // try 3 times
        for (int i = 0; i < 3; i++) {
          logger.log(Log.Level.trace, "scheduler await termination");

          if (scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
            logger.log(Log.Level.trace, "scheduler terminated");
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
      logAttributeNames(event);

      startup();

      this.web_inf = event.getServletContext().getRealPath("/WEB-INF");

      logger.array(Log.Level.info, "resources", getResourcePath());

      final AnnotationManager manager = new AnnotationManager();

      final List<String> files = manager.findClassFilesInPackage(AbstractImplementor.class);

      for (final String typename : files) {

        final Class<?> type = Class.forName(typename);

        if (type.isAnnotationPresent(GeneratorImplementor.class)) {

          final GeneratorImplementor ann = type.getAnnotation(GeneratorImplementor.class);

          logger.array(Log.Level.debug, "found annotation",
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
        logger.array(Log.Level.info, "default implementor", implementor.getClass().getName());
      }
      else {
        throw new RuntimeException("No default implementor found");
      }
    }
    catch (final Throwable t) {
      logger.exception(t);
    }
    finally {
      logger.out();
    }
  }

  /**
   *
   */


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
