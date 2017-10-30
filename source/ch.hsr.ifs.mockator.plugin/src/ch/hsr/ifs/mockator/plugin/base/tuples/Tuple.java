package ch.hsr.ifs.mockator.plugin.base.tuples;

public abstract class Tuple<Head, $> implements StringAppender {

   private final Head head;
   private final $    tail;

   protected Tuple(Head head, $ tail) {
      this.head = head;
      this.tail = tail;
   }

   public static <T1> Singleton<T1> from(T1 t1) {
      return new Singleton<T1>(t1);
   }

   public static <T1, T2> Pair<T1, T2> from(T1 t1, T2 t2) {
      return new Pair<T1, T2>(t1, t2);
   }

   public static <T1, T2, T3> Triple<T1, T2, T3> from(T1 t1, T2 t2, T3 t3) {
      return new Triple<T1, T2, T3>(t1, t2, t3);
   }

   public static <T1, Tail> T1 _1(Tuple<T1, Tail> tuple) {
      return tuple.head;
   }

   public static <T1, T2, Tail> T2 _2(Tuple<T1, Tuple<T2, Tail>> tuple) {
      return tuple.tail.head;
   }

   public static <T1, T2, T3, Tail> T3 _3(Tuple<T1, Tuple<T2, Tuple<T3, Tail>>> tuple) {
      return tuple.tail.tail.head;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) return false;

      if (!(obj instanceof Tuple)) return false;

      Tuple<?, ?> other = (Tuple<?, ?>) obj;
      return (head == null ? other.head == null : head.equals(other.head)) && tail.equals(other.tail);
   }

   @Override
   public int hashCode() {
      return (head == null ? 0 : head.hashCode()) + tail.hashCode() * 31;
   }

   @Override
   public String toString() {
      StringBuilder repr = new StringBuilder();
      repr.append("(").append(head);
      ((StringAppender) tail).appendString(repr, ", ");
      return repr.append(")").toString();
   }

   @Override
   public void appendString(StringBuilder buffer, String separator) {
      buffer.append(separator).append(head);
      ((StringAppender) tail).appendString(buffer, separator);
   }
}
