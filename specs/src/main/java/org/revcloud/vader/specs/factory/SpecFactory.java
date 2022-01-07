package org.revcloud.vader.specs.factory;

import org.revcloud.vader.specs.specs.Spec1;
import org.revcloud.vader.specs.specs.Spec2;
import org.revcloud.vader.specs.specs.Spec3;
import org.revcloud.vader.specs.specs.Spec4;

public final class SpecFactory<ValidatableT, FailureT> {

  @SuppressWarnings({"java:S100", "java:S1452"})
  public <GivenT> Spec1.Spec1Builder<ValidatableT, FailureT, GivenT, ?, ?> _1() {
    return Spec1.check();
  }

  @SuppressWarnings({"java:S100", "java:S1452"})
  public <WhenT, ThenT> Spec2.Spec2Builder<ValidatableT, FailureT, WhenT, ThenT, ?, ?> _2() {
    return Spec2.check();
  }

  @SuppressWarnings({"java:S100", "java:S1452"})
  public <WhenT, Then1T, Then2T>
      Spec3.Spec3Builder<ValidatableT, FailureT, WhenT, Then1T, Then2T, ?, ?> _3() {
    return Spec3.check();
  }

  @SuppressWarnings({"java:S100", "java:S1452"})
  public Spec4.Spec4Builder<ValidatableT, FailureT, ?, ?> _4() {
    return Spec4.check();
  }
}
