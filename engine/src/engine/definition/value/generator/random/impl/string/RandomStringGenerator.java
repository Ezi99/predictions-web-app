package engine.definition.value.generator.random.impl.string;

import engine.definition.value.generator.random.api.AbstractRandomValueGenerator;
import engine.definition.value.generator.random.impl.numeric.RandomIntegerGenerator;

public class RandomStringGenerator extends AbstractRandomValueGenerator<String> {

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!?,_-.() ";
    private final RandomIntegerGenerator strLengthGenerator;
    private final RandomIntegerGenerator indexGenerator;


    public RandomStringGenerator() {
        strLengthGenerator = new RandomIntegerGenerator(1, 50);
        indexGenerator = new RandomIntegerGenerator(0, CHARACTERS.length() - 1);
    }

    @Override
    public String generateValue() {
        StringBuilder stringBuilder = new StringBuilder();
        int strLength = strLengthGenerator.generateValue();

        for (int i = 0; i < strLength; i++) {
            int index = indexGenerator.generateValue();
            stringBuilder.append(CHARACTERS.charAt(index));
        }

        return stringBuilder.toString();
    }
}
