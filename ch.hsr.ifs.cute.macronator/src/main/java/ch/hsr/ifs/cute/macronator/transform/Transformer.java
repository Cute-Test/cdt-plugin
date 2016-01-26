package ch.hsr.ifs.cute.macronator.transform;

@FunctionalInterface
public interface Transformer {
    public abstract String generateTransformationCode();
    
    default boolean isValid() { 
        return true;
    }
}
