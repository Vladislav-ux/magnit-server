//package ru.magnit.demo.controllers;
//
//import ru.magnit.demo.entity.User;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class dfgf {
//    public static void main(String[] args) {
//        List<User> list = new ArrayList<>();
//        list.add(new User());
//        list.add(new User());
//        list.add(new User());
//        list.add(new User());
//        list.add(new User());
//        list.add(new User());
//        list.add(new User());
//        list.add(new User());
//
//        System.out.println(getLimitList(list, 7, 10));
//
//
//    }
//
//    private static List<User> getLimitList(List<User> list, int start_index, int end_index){
//        try{
//            return list.subList(start_index, end_index);
//        }catch (Exception e){
//            if(start_index < list.size()) {
//                return list.subList(start_index, list.size() - 1);
//            }
//            return null;
//        }
//    }
//}
