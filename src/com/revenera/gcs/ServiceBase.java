package com.revenera.gcs;

import com.flexnet.external.type.*;
import com.revenera.gcs.utils.Diagnostics.Token;
import com.revenera.gcs.utils.Log;
import com.revenera.gcs.utils.Utils;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;


public abstract class ServiceBase {

  protected final Log logger = Log.create(this.getClass());

  protected ServiceBase() {
    this.logger.in();
  }

  /**
   *
   * @param obj request payload from which to assess the FNO license technology name
   * @return FNO license technology name
   */
  protected String getLicenseTechnology(final Object obj) {
    final AtomicReference<LicenseTechnology> tech = new AtomicReference<>();

    if (obj instanceof ProductRequest) {
      tech.set(((ProductRequest) obj).getLicenseTechnology());
    }
    else if (obj instanceof LicenseModelRequest) {
      tech.set(((LicenseModelRequest) obj).getLicenseTechnology());
    }
    else if (obj instanceof GeneratorRequest) {
      tech.set(((GeneratorRequest) obj).getLicenseTechnology());
    }
    else if (obj instanceof ConsolidatedLicenseResquest) {
      ((ConsolidatedLicenseResquest) obj).getFulfillments().stream().findFirst().ifPresent(x -> {
        tech.set(x.getLicenseTechnology());
      });
    }
    else if (obj instanceof FulfillmentRecordSet) {
      ((FulfillmentRecordSet) obj).getFulfillments().stream().findFirst().ifPresent(x -> {
        tech.set(x.getLicenseTechnology());
      });
    }
    else if (obj instanceof RenewableEntitlementLineItems) {
      ((RenewableEntitlementLineItems) obj).getRenewableEntitlementLineItems().stream().findFirst().ifPresent(x -> {
        tech.set(x.getLicenseTechnology());
      });
    }
    else if (obj instanceof EntitlementLineItem) {
      tech.set(((EntitlementLineItem) obj).getLicenseTechnology());
    }
    else if (obj instanceof FulfillmentRecord) {
      tech.set(((FulfillmentRecord) obj).getLicenseTechnology());
    }
    else if (obj instanceof ConsolidatedLicenseRecord) {
      tech.set(((ConsolidatedLicenseRecord) obj).getLicenseTechnology());
    }

    if (tech.get() == null) {
      throw new RuntimeException(obj.getClass().getName() + " | cannot retrieve license technology");
    }
    else {
      this.logger.log(Log.Level.info, "license tech:" + tech.get().getName());
      return tech.get().getName();
    }
  }

  protected Token createDiagnosticsToken() {
    final StackTraceElement frame = Thread.currentThread().getStackTrace()[2];
    
    return Application.getInstance().getDiagnostics().getToken(this.getClass(), frame.getMethodName());
  }
  
  public Function<Throwable, SvcException> serviceException = (throwable) -> new SvcException() {
    {
      this.setMessage(Utils.frameDetails(Thread.currentThread().getStackTrace()[3]));
      
      this.setName(throwable.getClass().getName());
    }
  };
}