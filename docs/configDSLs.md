# Config DSL

Vader comes with 2 **ValidationConfig** types which have intuitive DSL methods to configure built-in validations and
specs. This config is passed to the runner method of Vader for execution

## ValidationConfig DSL

- Configure to validate Mandatory fields.
- Configure to validate Fields for SF ID format.

## BatchValidationConfig DSL

- Configure for **Filter & Fail** duplicate Items (In-Order)
- Configure for `AllOrNone` support
- Configure for min and max size validations
- Configure to validate Mandatory fields.
- Configure to validate Mandatory Fields for SF ID format.
- Configure to validate Non-Mandatory Fields for SF ID format.

## Specs

**(both the above config types support this)**

- Configure fluent low-code declarative **Specs** to replace data validations.
- Config DSLs are totally extensible to add more such use-cases.

### [👀 Sneek-Peek of Spec](specs.md)
