package ch.hsr.ifs.cute.swtbottest.util;

@FunctionalInterface
public interface Performer {
    void run();

    default Performer andThen(Performer after){
        return () -> {
            this.run();
            after.run();
        };
    }

    default Performer compose(Performer before){
        return () -> {
            before.run();
            this.run();
        };
    }
}