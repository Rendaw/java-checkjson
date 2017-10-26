CheckJson deserializes JSON into an object while checking annotated constraints aren't violated.  This uses Jackson
to tokenize the JSON but has it's own data binding code.  By default this is stricter than Jackson: every field is
required.  If constraints aren't met, CheckJson raises a `ValidationError`.

# Usage

```
class MyClass {
    @JsonProperty
    @Valid(min=Valid.Limit.INCLUSIVE, minValue=0)
    int aNumber;

    @JsonProperty
    @Valid(optional=true)
    String optionalValue = "default";
}

try {
    MyClass m = CheckJson.read(inputStream, MyClass.class);
} catch (ValidationError e) {
    System.out.format("Stream was invalid: %s\n", e.getMessage());
}
```

The `ValidationError` message is always user oriented - it will not contain references to internal classes or code.

# Current Limitations

Polymorphic members (interfaces) aren't supported.

All serialization occurs via non-final public fields, rather than getters/setters or constructors.
