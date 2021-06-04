# Specs

## 🌈 Spec-Spectrum

![inline](../images/spec-spectrum.png)

On a shared codebase, same algorithms get written in different styles. This leads to increase of entrophy, maintainance
and coverage overhead in a codebase. Can these common algorithmic patters be abstracted out into templates, with a way
to _Specify_ the missing pieces and reuse these generic validation algorithms?

> A **Spec** is a **Configuration**, which lets you reuse a well-tested algorithmic pattern/template for a validation, by specifying the placeholders to fill.

Let’s understand this with an example:

### A Validation through code:

```java
public static final SimpleValidator<ReferenceItemBaseInputRepresentation, ValidationFailure> billingMethodBillingTermCombo = inputRepresentation -> {
  final var billingTerm = inputRepresentation.getBillingTerm().getInternalName();
  final var billingMethod = inputRepresentation.getBillingMethod().getInternalName();

  return (!BillingValidationUtils.isValidBillingTermBillingMethodCombo(billingTerm, billingMethod))
    ? ofFieldIntegrity(getErrorMessageWithParams(
      BillingValidationErrorMessage.INVALID_BILLING_TERM_BILLING_METHOD_COMBO,
        new String[] { billingTerm, billingMethod }))
    : none();
};

public static boolean isValidBillingTermBillingMethodCombo(String billingTerm, String billingMethod) {
  return BillingCommonUtil.isBillingTermOneTime(billingTerm) && BillingCommonUtil.isBillingMethodOrderAmount(billingMethod) ||
    BillingCommonUtil.isBillingMethodEvergreen(billingMethod) && BillingCommonUtil.isBillingTermMonth(billingTerm);
}

public static boolean isBillingTermOneTime(String billingTerm) {
  return  BillingTerm.ONETIME.getDbValue().equals(billingTerm);
}

public static boolean isBillingMethodOrderAmount(String billingMethod) {
  return BillingMethod.ORDER_AMOUNT.getDbValue().equals(billingMethod);
}

public static boolean isBillingMethodEvergreen(String billingMethod) {
  return BillingMethod.EVERGREEN.getDbValue().equals(billingMethod);
}

public static boolean isBillingTermMonth(String billingTerm) {
  return BillingTerm.MONTH.getDbValue().equals(billingTerm);
}

// *** --- NEED MIN. 1 UNIT TEST PER BRANCH --- ***

@Test
void invalidBillingMethodBillingTermComboFails() {
  var inputRep = new ReferenceEntityItemInputRepresentation();
  inputRep.setBillingTerm(BillingTermEnum.OneTime);
  inputRep.setBillingMethod(BillingMethodEnum.Evergreen);     
  var expectedResult = ofFieldIntegrity(
      getErrorMessageWithParams(BillingValidationErrorMessage.INVALID_BILLING_TERM_BILLING_METHOD_COMBO, new String[]{
              String.valueOf(inputRep.getBillingTerm()),
              String.valueOf(inputRep.getBillingMethod())}));
  Assertions.assertEquals(expectedResult, ReferenceBaseItemValidator.billingMethodBillingTermCombo.unchecked().apply(inputRep));
}

@Test
void invalidBillingMethodBillingTermComboFails() {
  var inputRep = new ReferenceEntityItemInputRepresentation();
  inputRep.setBillingTerm(BillingTermEnum.OneTime);
  inputRep.setBillingMethod(BillingMethodEnum.Evergreen);     
  var expectedResult = ofFieldIntegrity(
        getErrorMessageWithParams(BillingValidationErrorMessage.INVALID_BILLING_TERM_BILLING_METHOD_COMBO, new String[]{
                String.valueOf(inputRep.getBillingTerm()),
                String.valueOf(inputRep.getBillingMethod())}));
  Assertions.assertEquals(expectedResult, ReferenceBaseItemValidator.billingMethodBillingTermCombo.unchecked().apply(inputRep));
}

@Test
void invalidBillingMethodBillingTermComboFails() {
  var inputRep = new ReferenceEntityItemInputRepresentation();
  inputRep.setBillingTerm(BillingTermEnum.OneTime);
  inputRep.setBillingMethod(BillingMethodEnum.Evergreen);     
  var expectedResult = ofFieldIntegrity(
      getErrorMessageWithParams(BillingValidationErrorMessage.INVALID_BILLING_TERM_BILLING_METHOD_COMBO, new String[]{
              String.valueOf(inputRep.getBillingTerm()),
              String.valueOf(inputRep.getBillingMethod())}));
  Assertions.assertEquals(expectedResult, ReferenceBaseItemValidator.billingMethodBillingTermCombo.unchecked().apply(inputRep));
}

@Test
void invalidBillingMethodBillingTermComboFails() {
  var inputRep = new ReferenceEntityItemInputRepresentation();
  inputRep.setBillingTerm(BillingTermEnum.OneTime);
  inputRep.setBillingMethod(BillingMethodEnum.Evergreen);     
  var expectedResult = ofFieldIntegrity(
        getErrorMessageWithParams(BillingValidationErrorMessage.INVALID_BILLING_TERM_BILLING_METHOD_COMBO, new String[]{
                String.valueOf(inputRep.getBillingTerm()),
                String.valueOf(inputRep.getBillingMethod())}));
  Assertions.assertEquals(expectedResult, ReferenceBaseItemValidator.billingMethodBillingTermCombo.unchecked().apply(inputRep));
}
```

#### Problems
- Writing tests for these validations take double the effort of writing code.
- For similar validations, we need to repeat/duplicate the same algorithmic structure and of-course 2X repetition for tests.
- In a shared code-based, multiple developers resort to different styles of writing the same type of validations, leading to a spike in *Cognitive Complexity*

> Instead of this ☝🏼, is there a way to just specify my validation, without writing any code?

### Checkout Specs:

**The above validation using a spec**

```java
spec._2().nameForTest(BILLING_TERM_BILLING_METHOD_COMBO_SPEC)        
  .when(ReferenceItemBaseInputRepresentation::getBillingMethod)        
  .then(ReferenceItemBaseInputRepresentation::getBillingTerm)        
  .shouldRelateWith(BILLING_METHOD_BILLING_TERM_COMBO)        
  .orFailWithFn((bt, bm) -> ofFieldIntegrity(getErrorMessageWithParams(INVALID_BILLING_TERM_BILLING_METHOD_COMBO, bt, bm)))

// ***!! NO FTESTS/UNIT TESTS REQUIRED FOR THESE SPECS !!*** 🤫
```
These Specs go into your DI config (Typically Spring Config on a Core module) and are handed over to Vader for execution
like below:

```java
ValidationConfig<Bean, ValidationFailure> validationConfig =
  ValidationConfig.<Bean, ValidationFailure>toValidate()
      .specify(spec -> List.of(spec._1()..., spec_1()..., spec_2()...)
      .prepare();
var results = validateAndFailFast(..., validationConfig);
```

#### 🤩 Wow!! The Spec speaks for itself 🤩

- No Code/Low code
- Uniform style in a shared code-base with **0 Cognitive complexity.**
- You get to reuse a well-tested algorithm behind the scene, [so you needn't write extra tests](#-specs-do-not-need-tests-).

---

## 🚨 Specs do NOT need Tests!!? 😱

Wait! Before you scream! Hear out the rationale behind this **Recommendation**. There are mainly two parts in this
feature:

- **What-to-do:** Specs
- **How-to-do:** How Vader checks your Specs, against your Bean/POJO

### 🧪Specs are just Configuration

Why do we cover validations code with tests?

- *I need Test all branches of my code*
  - But, a Spec has no logic. It’s only a configuration you provide to vader to execute the corresponding well-tested
    validation algorithm.
- *Tests act as a live documentation*
  * Specs are low-code and should be seen as plain sentences. So, they themselves serve as documentation.
- *Tests give us the confidence to refactor/change without breaking*
  - Specs work as they are written (unless you have a typo or a *Ctrl+C-Ctrl+V* issue 😉). So, the action “refactor”
    doesn’t apply to Specs.
- *Hey, but what if I really had a typo or I changed something unintentionally. I need tests to fail and alert me.*
  - Agreed! tests in these scenarios give us one extra layer of protection. But, test your Spec not the implementation
    behind it. For example:

    ```java
    spec._1().nameForTest(BDOM_RANGE_SPEC)
      .given(ReferenceItemBaseInputRepresentation::getBillDayOfMonth)
      .shouldMatch(anyOfOrNull(inRangeInclusive(1, 31)))
      .orFailWith(ofFieldIntegrity(INVALID_BILL_DAY_OF_MONTH))
    ```

    In a spec like this, typos can happen at places where you are providing hard-coded values like `1` or `31`, or much
    worse can happen at `given` or `orFailWith`. Write a test which _cross-checks_ this configuration, not the algorithm
    behind this Spec, cause it’s already well tested.

    >👋🏼 🔜 An out-of-the-box assertion API to declaratively test your spec configuration is coming-soon

  - Above that! are you convinced to double down your Dev efforts (by re-testing well-tested algorithms), just to cover
    Typos? Also, you have other layers of protection too, such as your dev testing, e2e tests, code-review, Blitz, etc.
    What’s the probability that this unfortunate typo slipped through all those layers?
  - As they are written in Java, you can even leverage Compiler for Correctness. It does all the job to make sure your
    data types align together. **So, if a Spec compiles it works as written.**

- *I am still not convinced, I need tests!*
  - First! please help us understand your concerns/use-cases by raising a git.soma issue. If possible, we shall cover
    your case and help you from the pain of writing tests.
  - And, why not! we just said you don’t need tests, but who said Specs are not testable? This is how simple it is to
    unit test them:

```java
@Test
public void invalidBillingTermFails() {
  var inputRep = new ReferenceEntityItemInputRepresentation();
	inputRep.setBillingTerm(BillingTermEnum.Quarter);
	assertFalse(referenceItemBatchValidationConfig.getSpecWithName(INVALID_BILLING_TERM_FAILS)
		.map(spec -> spec.test(inputRep)).orElse(true));
}
```

- For the specs that you want to test, you can name them with a DSL method `nameForTest` and use that name as above to
  call `getSpecWithName`. But think about it, **your tests are testing the vader's validation algorithm (which is
  already well-tested) and not your logic**, because there is no logic in Specs! 🤔

---

## What Spec do I need?

Currenly Vader provides 3 types of Specs and their names reflect the arity of fields they deal with. The declarative DSL
with intuitive method names should guide the develper to construct a Spec.

### Spec1

This deals with a single field. Use this spec if you wish to validate a field against one or more fields within the same
Validatable or one or more [Matchers](http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matchers.html). Vader
also provides some [Matchers out of the box](matchers.md). Example:

```java
spec._1().nameForTest(BILLING_TERM_SPEC)
  .given(ReferenceItemBaseInputRepresentation::getBillingTerm)
  .shouldMatch(anyOf(OneTime, Month))
  .orFailWith(ofFieldIntegrity(INVALID_BILLING_TERM))
```

```java
spec._1().nameForTest(BDOM_RANGE_SPEC)
  .given(ReferenceItemBaseInputRepresentation::getBillDayOfMonth)
  .shouldMatch(anyOfOrNull(inRangeInclusive(1, 31)))
  .orFailWith(ofFieldIntegrity(INVALID_BILL_DAY_OF_MONTH))
```

### Spec2

This deals with two inter-dependent fields. Their relation can be validated in three ways.

- By providing valid when-then Matchers.
- By providing a `Map` which acts as _Matrix_ of possible values for both the fields.
- By providing an assert function which takes these two fields as input.

```java
spec._2().nameForTest(END_DATE_BILLING_METHOD_SPEC_2)
  .when(ReferenceItemBaseInputRepresentation::getBillingMethod)
  .matches(is(OrderAmount))
  .then(ReferenceItemBaseInputRepresentation::getEndDate)
  .shouldMatch(notNullValue())
  .orFailWith(ofFieldIntegrity(INVALID_END_DATE_FOR_ORDER_AMOUNT))
```

```java
spec._2().nameForTest(BILLING_TERM_BILLING_TERM_UNIT_COMBO_SPEC)
  .when(ReferenceItemBaseInputRepresentation::getBillingTerm)
  .then(ReferenceItemBaseInputRepresentation::getBillingTermUnit)
  .shouldRelateWith(BILLING_TERM_BILLING_TERM_UNIT_COMBO)
  .orFailWithFn((bt, btu) -> ofFieldIntegrity(getErrorMessageWithParams(
    INVALID_BILLING_TERM_UNIT, bt, btu)))
```

```java
spec._2().nameForTest(START_DATE_END_DATE_SPEC)
  .when(ReferenceItemBaseInputRepresentation::getStartDate)
  .then(ReferenceItemBaseInputRepresentation::getEndDate)
  .shouldRelateWithFn(isOnOrBeforeIfBothArePresent())
  .orFailWith(ofFieldIntegrity(INVALID_START_AND_END_DATES))
```

### Spec 3

This deals with 3 inter-dependent fields. Based on the value of 1 field, the other 2 fields can be compared similar to
Spec 2.

```java
spec._3().nameForTest(BDOM_FOR_EVERGREEN_SPEC)
  .when(ReferenceItemBaseInputRepresentation::getBillingMethod)
  .matches(is(Evergreen))
  .thenField1(ReferenceItemBaseInputRepresentation::getBillDayOfMonth)
  .thenField2(ReferenceItemBaseInputRepresentation::getStartDate)
  .shouldRelateWithFn(isEqualToDayOfDate())
  .orFailWith(ofFieldIntegrity(INVALID_BILL_DAY_OF_MONTH_FOR_EVERGREEN))
```

```java
spec._3().nameForTest(BDOM_FOR_ORDER_AMOUNT_SPEC)
  .when(ReferenceItemBaseInputRepresentation::getBillingMethod)
  .matches(is(OrderAmount))
  .thenField1(ReferenceItemBaseInputRepresentation::getBillDayOfMonth)
  .thenField2(ReferenceItemBaseInputRepresentation::getStartDate)
  .shouldRelateWithFn(isEqualToDayOfDate())
  .orField1ShouldMatch(nullValue())
  .orFailWith(ofFieldIntegrity(INVALID_BILL_DAY_OF_MONTH_FOR_ORDER_AMOUNT)))
```

## Spec Orchestration

Constraints in each spec abide logical `OR`. Mutiple Specs can be composed for Fail-Fast by using `specify`
or `withSpecs` DSL function of `ValidationConfig`.

```java
public static final Specs<ReferenceItemBaseInputRepresentation, BillingScheduleFailure> bsSpecs = spec -> List.of(
  spec._1().nameForTest(BILLING_TYPE_SPEC)
  	.given(ReferenceItemBaseInputRepresentation::getBillingType)
  	.shouldMatch(is(Advance))
  	.orFailWith(ofFieldIntegrity(INVALID_BILLING_TYPE)),
  	// --- COMBO ---
  spec._2().nameForTest(BILLING_TERM_BILLING_METHOD_COMBO_SPEC)
  	.when(ReferenceItemBaseInputRepresentation::getBillingMethod)
  	.then(ReferenceItemBaseInputRepresentation::getBillingTerm)
  	.shouldRelateWith(BILLING_METHOD_BILLING_TERM_COMBO)
  	.orFailWithFn((bt, bm) -> ofFieldIntegrity(getErrorMessageWithParams(
      INVALID_BILLING_TERM_BILLING_METHOD_COMBO, bt, bm))),
  spec._3().nameForTest(BDOM_FOR_EVERGREEN_SPEC)
  	.when(ReferenceItemBaseInputRepresentation::getBillingMethod)
  	.matches(is(Evergreen))
  	.thenField1(ReferenceItemBaseInputRepresentation::getBillDayOfMonth)
  	.thenField2(ReferenceItemBaseInputRepresentation::getStartDate)
  	.shouldRelateWithFn(isEqualToDayOfDate())
  	.orFailWith(ofFieldIntegrity(INVALID_BILL_DAY_OF_MONTH_FOR_EVERGREEN)));
}

// Hook these to validation config
BatchValidationConfig.<ReferenceItemBaseInputRepresentation, BillingScheduleFailure>toValidate()
  .specify(bsSpecs);
```

---

### `@Annotation` vs `Spec`

Annotations are reflection based and they create a lot of *runtime magic*. They are not bad in-general, but using them
for validations has these cons:

* It's difficult to debug as you wouldn't know which `AnnotationProcessor` handles which `@Annotation` unless the
  Javadoc writer of that Annotation is gracious to provide those details.
* With Annotations, You can't use a simple *⌘+Click* to know what's going on underneath anymore. But Specs are totally
  transparent both at compile and runtime.
* Annotations are spread across your Beans and code-search is the only way to find them. With specs, all your
  validations are at one place. Easy Maintainability!
* Annotations offer limited type-safety. It’s not possible to specify contextual requirements. Any annotation can be
  placed on any type. With Specs, Compiler is your friend.
* Use of Reflections for Annotations also incur a runtime cost.
* Finally, If you are thinking about tests, forget Annotations!

### How can I trust Vader with my specs?

* **Well, coz that’s the bread-&-jam of what we promise.**
* It’s our responsibility to address all the edge cases and automate them thoroughly, for various types of POJOs.
* We take code-quality & security seriously. **This code base has integration with SonarQube.** **We openly display
  our [Code-Quality Shields](https://docs.sonarqube.org/latest/user-guide/metric-definitions/) on top of
  our [git.soma repo](http://git.soma.salesforce.com/CCSPayments/vader).** (Note: This repo is under active-development
  for 2.0 and the accumulated tech-debt will soon be covered)
* That said, just like us, Vader matures day-by-day, and if any of your special use-cases don’t work, we shall love to
  fix them ASAP (TBD - Will publish SLA for P0, P1 etc).

### How can I write complex validations with specs?

* If your validation is complex, think through, if it can be broken down into simple sentences that you can write on a
  document. As long as that is possible, you can translate them to Specs.
* If that’s not possible, you may write it as  `Validator/SimpleValidator` and mix it with your specs.

### How did this idea origin?

When writing tests for validations using this awesome library **AssertJ**, the tests felt more declarative than the
actual validation code. This gave rise to the idea, why not make the actual validations declarative, so they don’t need
tests.

#### Notes:

Specs feature is under active development. If you are an early-adopter and wish to migrate your validations to Specs,
although the end-goal is “No Tests for Specs”, we advise you to keep your tests, point them to these specs and kindly
report us any bugs or missing use-cases. We keep covering them as we mature.

