package ru.magnit.demo;

import ru.magnit.demo.entity.User;

public class Main {
    public static void main(String[] args) {
        System.out.println(compare("aaaaa", "bbbb"));
        System.out.println(compare(null, null));
        System.out.println(compare(null, "bbbb"));
        System.out.println(compare("aaaaa", null));

    }

    public static int compare(String name1, String name2){
        if(name1 == null && name2 == null){
            return 0;
        }else if(name1 == null){
            return 1;
        }else if(name2 == null){
            return -1;
        }
        return name1.compareTo(name2);
    }
}
