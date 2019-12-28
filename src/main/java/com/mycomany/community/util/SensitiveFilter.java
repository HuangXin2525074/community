package com.mycomany.community.util;


import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);


    private static final String REPLACEMENT="***";


    // start from root.
    private TrieNode rootNode = new TrieNode();

    @PostConstruct
    public void init(){

     try( InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      ){
    String keyword;
    while((keyword = reader.readLine())!=null){
        this.addKeyword(keyword);

    }

      } catch(IOException e){
    logger.error("input sensitive words fail",e.getMessage());
       }

    }

    private void addKeyword(String keyword){
        TrieNode tempNode =rootNode;

        for(int i=0; i<keyword.length();i++){
            char c = keyword.charAt(i);
           TrieNode subNode = tempNode.getSubNode(c);
           if(subNode ==null){
               // init
               subNode = new TrieNode();
               tempNode.addSubNode(c,subNode);
           }

           // move to next Node
           tempNode = subNode;

           if(i== keyword.length()-1){
               tempNode.setKeywordEnd(true);
           }

        }
    }



    public String filter(String text){
        if(StringUtils.isBlank(text)){
            return null;
        }

        // pointer1
        TrieNode tempNode = rootNode;

        //pointer2
        int begin=0;
        //pointer3
        int position=0;

        StringBuilder sb = new StringBuilder();

        while(begin < text.length()){
            char c = text.charAt(position);

            if(isSymbol(c)){
                if(tempNode == rootNode){
                    sb.append(c);
                    begin++;
                }
                position++;
                continue;
            }

           tempNode = tempNode.getSubNode(c);
            if(tempNode == null){
                sb.append(text.charAt(begin));

                // move to next position;
                position= ++begin;

                tempNode = rootNode;
            }else if(tempNode.isKeywordEnd()){

                sb.append(REPLACEMENT);

               begin = ++position;

               tempNode = rootNode;

            }else{
                if(position <text.length()-1){
                    position++;
                }
            }

        }

        sb.append(text.substring(begin));

        return  sb.toString();

    }


    private boolean isSymbol(Character c){
        return !CharUtils.isAsciiAlphanumeric(c);
    }





    // Trie tree class.
    private class TrieNode{

      private boolean isKeywordEnd = false;

      private Map<Character,TrieNode> subNodes = new HashMap<>();


        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        public void addSubNode(Character c, TrieNode node){
            subNodes.put(c,node);
        }

        public TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }
    }



}
