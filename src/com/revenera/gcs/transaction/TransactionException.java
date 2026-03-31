package com.revenera.gcs.transaction;

import com.revenera.gcs.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TransactionException extends RuntimeException {

  private static void assertNow(final String... parameters) {

    final List<String> list = new ArrayList<>();

    list.addAll(Arrays.asList(parameters));
    list.addAll(Arrays.asList(Utils.frameDetails.apply(4)));

    throw new TransactionException(String.join(" | ", list));
  }

  public TransactionException(final String message) {
    super(message);
  }

  public static void assertNotNull(final Object obj) {
    if (Objects.isNull(obj)) {
      assertNow("null value not allowed");
    }
  }

  public static void assertNull(final Object obj) {
    if (Objects.nonNull(obj)) {
      assertNow("null value required", obj.getClass().getSimpleName());
    }
  }
}
