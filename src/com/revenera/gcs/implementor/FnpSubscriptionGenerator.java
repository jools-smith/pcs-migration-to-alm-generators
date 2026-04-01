package com.revenera.gcs.implementor;

import com.flexnet.external.type.*;
import com.flexnet.external.webservice.keygenerator.LicGeneratorException;
import com.revenera.gcs.Application;
import com.revenera.gcs.utils.GeneratorImplementor;
import com.revenera.gcs.utils.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

class Resources {
  final String filename;

  Resources(final String technology) {
    this.filename = String.format("%s.%08X.license", technology, Instant.now().toEpochMilli());
  }

  Path getLicenseFilepath() {
    return Application.getInstance().getResourcePath("licenses", filename);
  }

  Path getWorkingDirectory() {
    return Application.getInstance().getResourcePath("licenses");
  }

  Path getExecutablePath() {
    return Application.getInstance().getResourcePath("executable", "Test.exe");
  }
}

@SuppressWarnings("unused")
@GeneratorImplementor(technology = "FNPS", isDefault = false)
public class FnpSubscriptionGenerator extends AbstractImplementor {

  private final static String licenseFilename = "license";

  @Override
  public String technologyName() {
    return "FNP Subscription License Technology";
  }

  @Override
  public String technologyId() {
    return "FNPS";
  }

  @Override
  public GeneratorResponse generateLicense(final GeneratorRequest request) throws LicGeneratorException {
    logger.in();

    try {
      final Resources res = new Resources(technologyId());

      logger.json(Log.Level.debug, request);

      final Path file = Files.createFile(res.getLicenseFilepath().toAbsolutePath());
      logger.array(Log.Level.debug,"license file path", file.toAbsolutePath());

      final ProcessBuilder pb = new ProcessBuilder(
          res.getExecutablePath().toAbsolutePath().toString(),  // executable path
          file.toAbsolutePath().toString(),
          "================================",
          "Vendor Name: coriolis",
          "Product: FlexNet Publisher",
          "Version: 11",
          "Platforms: ALL",
          "TRL: \"Y\"",
          "--------------------------------",
          "#define VENDOR_KEY1 0xac361887",
          "#define VENDOR_KEY2 0x25621cfd",
          "#define VENDOR_KEY3 0x744a9c30",
          "#define VENDOR_KEY4 0x3d11d20b",
          "#define VENDOR_KEY5 0x63702b83",
          "#define VENDOR_NAME \"coriolis\"",
          "\"coriolis\" 0x6ff706a1 0x896f965e"
      );

      pb.directory(res.getWorkingDirectory().toFile());

      final Process proc = pb.start();
      logger.log(Log.Level.debug, "started process");

      final boolean status = proc.waitFor(30, TimeUnit.SECONDS);
      logger.log(Log.Level.debug, "finished process");

      try (final BufferedReader reader =
               new BufferedReader(
                   new InputStreamReader(
                       Files.newInputStream(file, StandardOpenOption.READ, StandardOpenOption.DELETE_ON_CLOSE)))) {

        return new GeneratorResponse() {
          {
            this.licenseFiles = Collections.singletonList(new LicenseFileMapItem() {
              {
                name = licenseFilename;
                value = reader.lines().collect(Collectors.joining("\n"));
              }
            });

            this.complete = true;

            // debug
            this.licenseFiles.forEach(file -> logger.array(Log.Level.debug, file.getName(), file.getValue()));
          }
        };
      } // file is deleted here
    }
    catch (final Throwable t) {
      logger.exception(t);

      throw new RuntimeException(t);
    }
  }

  @Override
  public ConsolidatedLicense consolidateFulfillments(final FulfillmentRecordSet request) throws LicGeneratorException {

    logger.array(Log.Level.debug, Application.getBuildVersion().getDate(), Application.getBuildVersion().getSequence());

    return new ConsolidatedLicense() {
      {
        this.fulfillments = request.getFulfillments();

        this.licFiles = Collections.singletonList(new LicenseFileMapItem() {
          {
            this.name = licenseFilename;

            this.value = request.getFulfillments().stream()
                .flatMap(fid -> fid.getLicenseFiles().stream())
                .filter(file -> file.getName().equals(licenseFilename))
                .map(file -> file.getValue().toString())
                .collect(Collectors.joining("\n"));
          }
        });

        // debug
        this.licFiles.forEach(file -> logger.array(Log.Level.debug, file.getName(), file.getValue()));
      }
    };
  }
}
