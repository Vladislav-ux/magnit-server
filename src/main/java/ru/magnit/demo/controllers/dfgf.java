//package ru.magnit.demo.controllers;
//
////import ru.magnit.demo.entity.User;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class dfgf {
//    public static void main(String[] args) {
//        List<User> list = new ArrayList<>();
//        list.add(new User("0"));
//        list.add(new User("1"));
//        list.add(new User("2"));
//        list.add(new User("3"));
//        list.add(new User("4"));
//        list.add(new User("5"));
//        list.add(new User("6"));
//        list.add(new User("7"));
//
//        System.out.println(getLimitList(list, 0, 7) + " ");
//        System.out.println(getLimitList(list, 3, 6) + " ");
//        System.out.println(getLimitList(list, 4, 4) + " ");
//        System.out.println(getLimitList(list, 10, 11) + " ");
//        System.out.println(getLimitList(list, 5, 15) + " ");
//
//
//
//    }
//
//    private static List<User> getLimitList(List<User> list, int start_index, int end_index){
//
//        if(list == null){
//            return null;
//        }
//
//        if(end_index < list.size()){
//            return list.subList(start_index, end_index + 1);
//        }else if(start_index < list.size()){
//            return list.subList(start_index, list.size());
//        }else{
//            return null;
//        }
//
////        try{
////            return list.subList(start_index, end_index);
////        }catch (Exception e){
////            if(start_index < list.size()) {
////                return list.subList(start_index, list.size() - 1);
////            }
////            return null;
////        }
//    }
//
//    static class User{
//        public String name;
//
//        public User(String name) {
//            this.name = name;
//        }
//
//        @Override
//        public String toString() {
//            return "User{" +
//                    "name='" + name + '\'' +
//                    '}';
//        }
//    }
//}
