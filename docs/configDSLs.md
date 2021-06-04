# Config DSL

**Config** acts as a specification for your Bean or Batch-of-Beans. Vader comes with 3 **ValidationConfig** types which have intuitive DSL 
methods to configure built-in validations and specs. This config is passed to the runner method of Vader for execution.

## HeaderValidationConfig DSL

You can use this to configure all header-level validations. This is helpful especially, when you are validating a Batch.

- Configure min and max size of your batch by specifying `batchMapper`. This supports multiple batches under a header.
- Configure other validators on Header bean, written using one of `Validator*` types.

## ValidationConfig DSL

- Configure to validate Mandatory fields.
- Configure to validate Fields for SF ID format.
- Configure other validators for the bean, written using one of `Validator*` types.

## BatchValidationConfig DSL

- Configure to **Filter & Fail** duplicate Items (In-Order)
- Configure to validate Mandatory fields.
- Configure to validate Mandatory Fields for SF ID format.
- Configure to validate Non-Mandatory Fields for SF ID format.
- Configure other validators for item-level beans, written using one of `Validator*` types.

## Specs

**(both the above config types support these)**

- Configure fluent low-code declarative **Specs** to replace data validations.
- Config DSLs are totally extensible to add more such use-cases.

### [👀 Sneak-Peek of Spec](specs.md)
