package ch.hsr.ifs.mockator.plugin.base.collections;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Stream;


// java.util.Stack is seriously flawed, so I use a java.util.Deque
// here for my purposes (there is even a note in the JavaDoc of java.util.Stack
// that suggests this)
public class MyStack<T> {

   private final Deque<T> stack;

   public MyStack() {
      stack = new ArrayDeque<>();
   }

   public void push(final T node) {
      stack.addFirst(node);
   }

   public T pop() {
      return stack.removeFirst();
   }

   public T peek() {
      return stack.getFirst();
   }

   public void clear() {
      stack.clear();
   }

   public boolean isEmpty() {
      return stack.isEmpty();
   }

   public int size() {
      return stack.size();
   }

   public Stream<T> stream() {
      return stack.stream();
   }

   public Stream<T> parallelStream() {
      return stack.parallelStream();
   }
}
