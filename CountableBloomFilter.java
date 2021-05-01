package ru.kpfu.itis.daniyar.idrisov.datamining;

import org.apache.commons.codec.digest.DigestUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CountableBloomFilter {

    private static final double FALSE_POSITIVE = 0.001;

    private int countOfItems;

    private int sizeOfFilter;

    private int countOfHash;

    private String url;

    private int [] arrayOfBits;

    private String [] arrayOfWords;

    public CountableBloomFilter(String url) {
        this.url = url;
        this.arrayOfWords = getWordsFromWebsite();
        this.countOfItems = getCountOfItems();
        this.sizeOfFilter = getSizeOfFilter();
        this.countOfHash = getCountOfHash();
        this.arrayOfBits = new int[sizeOfFilter];
        this.addWordsToBitArray();
    }

    private String [] getWordsFromWebsite() {
        Document document = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            document = Jsoup.connect(this.url).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Elements allWords = document.getAllElements();
        for (Element element: allWords.select("a")) {
            stringBuilder.append(element.text() + " ");
        }
        String [] arrayOfWords = stringBuilder.toString().split(" ");
        return arrayOfWords;
    }

    private int getCountOfItems() {
        return this.arrayOfWords.length;
    }

    private int getSizeOfFilter() {
        return (int) (-(countOfItems * Math.log(FALSE_POSITIVE)) / (Math.pow(Math.log(2), 2)));
    }

    private int getCountOfHash() {
        return (int) Math.ceil((this.sizeOfFilter / this.countOfItems) * Math.log(2));
    }

    private void addWordsToBitArray() {
        for (int j = 0; j < arrayOfWords.length; j++) {
            for (int i = 0; i < this.countOfHash; i++) {
                String hashWord = DigestUtils.md2Hex(arrayOfWords[j]);
                long longHashWord = 1;
                for (byte byteOfHashWord: hashWord.getBytes(StandardCharsets.UTF_8)) {
                    longHashWord = longHashWord * byteOfHashWord;
                }
                longHashWord = Math.abs(longHashWord) % this.sizeOfFilter;
                arrayOfBits[(int) longHashWord] = arrayOfBits[(int) longHashWord] + 1;
            }
        }
    }

    public boolean isContainsWord(String word) {
        for (int i = 0; i < this.countOfHash; i++) {
            String hashWord = DigestUtils.md2Hex(word);
            long longHashWord = 1;
            for (byte byteOfHashWord: hashWord.getBytes(StandardCharsets.UTF_8)) {
                longHashWord = longHashWord * byteOfHashWord;
            }
            longHashWord = Math.abs(longHashWord) % this.sizeOfFilter;
            if (arrayOfBits[(int) longHashWord] != 0) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        CountableBloomFilter countableBloomFilter = new CountableBloomFilter("https://study.istamendil.info");
        System.out.println(countableBloomFilter.isContainsWord("Java"));
        System.out.println(countableBloomFilter.isContainsWord("Даник"));
        System.out.println(countableBloomFilter.isContainsWord("Spring"));
        System.out.println(countableBloomFilter.isContainsWord("MVC"));
        System.out.println(countableBloomFilter.isContainsWord("Брюйне"));
        System.out.println(countableBloomFilter.isContainsWord("WWW"));
        System.out.println(countableBloomFilter.isContainsWord("Ronaldo"));
        System.out.println(countableBloomFilter.isContainsWord("DataMining"));
        System.out.println(countableBloomFilter.isContainsWord("JavaScript"));
        System.out.println(countableBloomFilter.isContainsWord("GitHub"));
    }

}