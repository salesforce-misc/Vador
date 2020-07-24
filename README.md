# 🦾 Vader 🦾

![inline](images/vader.png)

---

# Why a new Framework for Bean validation?

When it comes to Bean validation, [Spring Bean Validation](https://reflectoring.io/bean-validation-with-spring-boot/) is the most popular framework. But it comes with a lot of *Spring-baggage* and does a lot of *Annotation-Magic*. Although this is simple, the simplicity comes at the cost of losing a lot of flexibility. It works well for simple Data validations like checking Min/Max, but as the validations get complex, the code turns convoluted and it gets difficult to manage all those validations popping up as annotations across your Domain objects.

There is no way to configure the order of validations, which is required when you need a *Fail-Fast* strategy and want to load validations in the ascending order of their cost. Especially, if you service accepts a batch of requests (A Batch service), it gets really difficult to manage partial failures.

### How is Salesforce core dealing with this?

Predominantly with `if-else-try-catch` pyramids, which can spike your Cyclomatic & Cognitive complexities and renders a *difficult-to-test-and-manage* code-base.

### Prove it!

Watch this Tech-talk as a prerequisite to understand why `if-else-try-catch` is easy to start but difficult to manage and how Vader disciplines your code-base:

Proposal: https://overfullstack.github.io/posts/fight-complexity-with-fp/

Video: [Fight Complexity with Functional Programming, JavaBin, Norway](https://youtu.be/tnpL1O8kTbM)

---

# What Requirements does Vader cater?

You may consider using this framework if your bean validation has following requirements:

- Configure the order of Validations
- Cross-Share Common & Nested Validations among services
- Support Fail-Fast and Error-Accumulation Strategies
- Capture Partial failures (In Batch Services)
- Cater both non-Batch and Batch services

- Meta-Requirements
  - Accommodate a century validations
  - Unit testability
  - No compromise on Performance

---

## 🍢 Vader is just a Bunch of Design-Patterns stitched together 🍡

### Visitor/Strategy + Chain of Responsibility + Builder + Monad


Vader's Implementation is agnostic of the consumer's --

- Request Type  
- Failure Type
- Framework used

---

# How do I consume Vader?

- Write you validations as First-Class Functions.
- Stitch your validations together into a Chain.
- Call the API function as per the execution strategy.

Let's deep dive how Vader can help you in each of these steps:

---

# First-Class Validation?

### Job of a validation is simple, just to convey if a POJO is valid or why it is invalid.

### Vader provides various **Validator Types**, to get this done.

---

# Validator Types

These are generic data types to which a lambda can be assigned. They are called *Simple* as they work with Simple types as Input/Output

### SimpleValidator `(ValidatableT) -> FailureT`

Data type for simple first-class functions. They take in a bean to be validated, represented by `ValidatableT` and returns a failure `FailureT`.

```java
public static final SimpleValidator<Parent, ValidationFailure> validation1 =
            parentInputRepresentation -> {
                if (parentInputRepresentation._isSetPaymentAuthorizationId()) {
                    return null;
                } else {
                    return new ValidationFailure(ApiErrorCodes.REQUIRED_FIELD_MISSING, FIELD_NULL_OR_EMPTY,
                            ERROR_LABEL_PARAM_PAYMENT_AUTHORIZATION_ID);
                }
            };
```

### SimpleThrowableValidator `(ValidatableT) -> FailureT` (throws UncheckedException)

This is same as `SimpleValidator` but the body of the function does some side-effects (DB calls/Network calls) which are exception prone, i.e., it may throw an unchecked exception. Using this Data type for any lambda avoids using `try-catch` boilerplate inside the method body and Vader takes care of it. The how part will be covered in the DSL section.

```java
static final SimpleThrowableValidator<Parent, ValidationFailure> validationThrowable1 =
            parentInputRepresentation -> {
                throw new IllegalArgumentException("1 - you did something illegal");
            };
```

---

# But if you need more Power ⚡️⚡️

![inline](images/more-power.gif)

---

# Validator Types

Unlike Simple validation types (which work with Simple input/output types), these Data types work with `Either` types as input/output. The `Either` type is borrowed from `Vavr` library. More about it can be read [here](https://www.vavr.io/vavr-docs/#_either).

### But what's so powerful about `Either`?

With Either, You get all the functional programming powers. You can write linear programs with a lot less **Cyclomatic Complexity** & **Cognitive Complexity**.

Please refer to this tech talk discussing these concepts: [Fight Complexity with Functional Programming, JavaBin, Norway](<https://youtu.be/tnpL1O8kTbM>)

### Validator `Either<FailureT, ValidatableT> -> Either<FailureT, ?>`

Functions assigned to these Data types take `Either<FailureT, ValidatableT>` as input. Since the input is pre-wrapped in an `Either`, you can perform all the `Either` operations on the input like `map`, `flatMap`, `fold`, `filterOrElse` (Refer [API](https://www.javadoc.io/doc/io.vavr/vavr/0.10.2/io/vavr/control/Either.html) for more info). 

The result of the function is supposed to be `Either<FailureT, ?>`. This signifies, if there is a Validation Failure, keep it in the *left* state. If the `Either` in the result is in *right* state, it is considered that the bean **Passed** the validation. The wildcard `?` signifies, it doesn't matter what is the value in the right state.

```java
public static final Validator<Parent, ValidationFailure> batchValidation1 =
            parentInputRepresentation -> parentInputRepresentation
                    .filterOrElse(Parent::_isSetAccountId, ignore -> new ValidationFailure(
                            ApiErrorCodes.REQUIRED_FIELD_MISSING,
                            FIELD_NULL_OR_EMPTY,
                            ERROR_LABEL_PARAM_PAYMENT_AUTHORIZATION_ID));
```

### ThrowableValidator

Handling exceptions with `try-catch` hinders the linear program flow, and add extra branch adding-up to the **Cognitive Complexity**. But if the function is assigned to this Data type, you can omit all the exception handling and delegate that to the Vader. We shall see in the DSL section how this can be achieved.

```java
public static final ThrowableValidator<Parent, ValidationFailure> batchValidationThrowable1 =
            parentInputRepresentation -> {
                throw new IllegalArgumentException("1 - you did something illegal");
            };
```

---

# What Data types to Use?

These 4 types that Vader provides, is to help developers focus only on their validation logic, not worry about boiler-plate and use a programming style (imperative or functional) that they are comfortable in. You can essentially use any Data type for your validators and in-fact you can even have a mix, based on your needs. Vader leaves the choice to the developer. If you are using a mix, there's a little *Stitching* job you need to perform, which we shall see in the DSL section.

---

# Types of Validations in a Service

![inline](images/validation-classification.png)

- Non-Simple refers to `Validator` type 
- Parent refers to a Bean which consists of Nested beans
- Child refers to a nested bean inside a Parent

---

# How to Stitch all these in config 🏋🏻‍♂️

Using these Data types for lambdas, we essentially use functions as values. So, all we need is an Ordered List (like `java.util.list`) to maintain the sequence of validations. We can compose all the validation functions, in the order of preference. This order is easily **Configurable**.

However, there is a complexity. List of Validations for a parent node consists of a mix of parent node and child node validations. But they can't be put under one `List`, as they are functions on different Data Types. So child validations need to be ported to parent context. We can achieve this with **Higher-Order Functions**, which act as DSL to **lift** child validation to parent type.

This is a powerful technique, which enables us to see the code through the lens of **Algebra**. This way, we can configure a **Chain** of validations in-order, sorting out all the parent-child dependencies. This is nothing but the most popular **Chain of Responsibility** Design pattern, with a functional touch.

If the inter-dependencies between Parent-Child happens to be more complex, we may end-up with *Graph* like relationship, but it can easily be *flatten* into a Chain with simple *Topological Sort*.

Similarly, Vader has DSL to port Simple Validator types to Non-Simple ones. This is handy, when you have a mix of validations, and they all need to be of the same type in order to stitch them together.

Below are the DSLs currently available, with self-explanatory names:

|                            |                               |
| -------------------------- | ----------------------------- |
| liftToParentValidationType | liftAllToParentValidationType |
|                            |                               |

| **Simple**    | **Simple Throwable**   | **Throwable**    |
| ------------- | ---------------------- | ---------------- |
| liftSimple    | liftSimpleThrowable    | liftThrowable    |
| liftAllSimple | liftAllSimpleThrowable | liftAllThrowable |

---

# Run Strategies

Now that we saw how to write validations and how to stitch them together in any order, the last step left is to utilize an out-of-the box runner to run all these validations against one or batch of validatables. Vader has a **RunnerDSL**, which has API methods for below strategies. There are various flavors of these strategies for Batch/Non-Batch and Simple/Non-Simple.

### `validateAndFailFast`

### `validateAndAccumulateErrors`

---

# Future Roadmap

- Parallel runner to run validations in parallel

- Timeouts for validations

---



![inline](images/vader.gif)

---

# Source-code Setup

- This is a simple maven project, so all you need is maven on your system. As of writing this, Maven 3.6.3 is used.

- **[lombok](https://projectlombok.org/)** is used to generate boiler plate code. There are plugins available for lombok for all popular IDEs, which you need to install. Latest version of the plugin should work.

- Java version used for both source and target - openjdk-1.8.0_212

---

# Jar release process

- You need to have necessary permissions to release jar into Nexus for both SNAPSHOTS and RELEASE channels. This Gus item was raised for Nexus access - https://gus.my.salesforce.com/a07B0000007Qt0BIAS. Please reach out to this team for any Nexus related permissions, problems or requirements.

- Run the below command to upgrade the version of the library and push it to Nexus. This command should walk you through few steps where you may stick with default values.

  ```shell
  mvn release:prepare release:perform
  ```

  > ⚠️ This fails for the first time. Open `release.properites` file and replace the property `scm.url` with this line `scm.url=scm\:git\:ssh\://git@git.soma.salesforce.com/ccspayments/batch-validation-framework`. Run this command again, it resumes from where it failed and should be successful.

- After the build is successful, go to the [Nexus Repo](https://nexus.soma.salesforce.com/nexus/index.html#welcome) and verify if the current version is uploaded.
