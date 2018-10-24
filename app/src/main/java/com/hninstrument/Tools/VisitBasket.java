package com.hninstrument.Tools;

import java.util.HashSet;
import java.util.Set;

public class VisitBasket {
    Set<String> inSideSet = new HashSet<String>();
    Set<String> outSideSet = new HashSet<String>();

    public void add(String cardId,Callback callback){
        if(inSideSet.add(cardId)){
            callback.dataUpload();
        }else{
            if(outSideSet.add(cardId)){
                callback.dataUpload();
            }else{
                callback.TextBack();

            }
        }
    }
    public boolean empty(){
        return inSideSet.size()-outSideSet.size() <=0;
    }

    public void clear(){
        inSideSet.clear();
        outSideSet.clear();
    }

    public interface Callback{
        void dataUpload();
        void TextBack();

    }

}
