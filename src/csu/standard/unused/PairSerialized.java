package csu.standard.unused;
//package csu.standard;
//
//import java.io.Serializable;
//
///**
// * Copied from MRL
// * User: CSU
// * Date: 09/23/2013
// * Time: 00:02AM
// */
//public class PairSerialized<S, T> implements Serializable {
//    static final long serialVersionUID = -198713768237652370L;
//    private S first;
//    private T second;
//
//    public PairSerialized(S first, T second) {
//        this.first = first;
//        this.second = second;
//    }
//
//    public PairSerialized() {
//    }
//
//    public S first() {
//        return first;
//    }
//
//    public void setFirst(S first) {
//        this.first = first;
//    }
//
//    public T second() {
//        return second;
//    }
//
//    public void setSecond(T second) {
//        this.second = second;
//    }
//
//    @Override
//    public String toString() {
//        return "PairSerialized{" +
//                "first=" + first +
//                ", second=" + second +
//                '}';
//    }
//
//    @Override
//    public int hashCode() {
//        int hashCode = super.hashCode();
//        if (first == null && second != null) {
//            hashCode = second.hashCode();
//        } else if (second == null && first != null) {
//            hashCode = first.hashCode();
//        } else if (second == null && first == null)
//            return hashCode;
//
//
//        return first.hashCode() + second.hashCode();
//    }
//
//    public boolean equals(java.lang.Object o) {
//        if (o == null) return false;
//        if (o instanceof PairSerialized) {
//            PairSerialized<?, ?> p = (PairSerialized<?, ?>) o;
//
//            if ((first != null && first.equals(p.first())) && (second != null && second.equals(p.second()))) {
//                return true;
//            } else {
//                if (first == null && p.first() == null && (second != null && second.equals(p.second()))) {
//                    return true;
//                } else {
//                    if (second == null && p.second() == null && (first != null && first.equals(p.first()))) {
//                        return true;
//                    } else {
//                        if (first == null && p.first() == null && second == null && p.second() == null) {
//                            return true;
//                        }
//                    }
//
//                }
//            }
//        }
//        return false;
//
//    }
//}
