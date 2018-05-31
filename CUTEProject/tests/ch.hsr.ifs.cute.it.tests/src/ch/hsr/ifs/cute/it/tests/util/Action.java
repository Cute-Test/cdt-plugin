package ch.hsr.ifs.cute.it.tests.util;

// Copied from: https://stackoverflow.com/a/45419418

@FunctionalInterface
public interface Action {

   void run();

   default Action andThen(Action after) {
      return () -> {
         this.run();
         after.run();
      };
   }

   default Action compose(Action before) {
      return () -> {
         before.run();
         this.run();
      };
   }
}
