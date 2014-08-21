package de.fau.cs.mad.yasme.android.contacts;

import de.fau.cs.mad.yasme.android.entities.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Stefan on 20.06.14.
 */
public class ContactListContent {

    public List<ContactListItem> items = new ArrayList<ContactListItem>();

    public static Map<String, ContactListItem> ITEM_MAP = new HashMap<String, ContactListItem>();

    private List<Map<String,String>> listMap;

    public ContactListContent(){
        listMap = new ArrayList<>();
    }


    public void addItem(ContactListItem item){
        Map<String,String> map = new HashMap<String,String>(2);
        map.put("name", item.content);
        map.put("mail", item.subContent);
        listMap.add(map);
        items.add(item);
    }

    public List<Map<String,String>> getMap(){
        return listMap;
    }

    public void clearItems(){
        listMap.clear();
        items.clear();
    }

    public static class ContactListItem {
        public String id;
        public String content;
        public String subContent;
        public User user;

        public ContactListItem(String id, String content, String subContent) {
            this.id = id;
            this.content = content;
            this.subContent = subContent;
        }
        public ContactListItem(String id, String content, String subContent, User user) {
            this.id = id;
            this.content = content;
            this.subContent = subContent;
            this.user = user;
        }

        @Override
        public String toString() {
            return content;
        }
    }

}
