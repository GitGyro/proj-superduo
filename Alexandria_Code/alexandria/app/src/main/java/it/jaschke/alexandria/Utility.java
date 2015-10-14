
package it.jaschke.alexandria;

/**
 * Created by Aditya on 10/2/15.
 */
public class Utility {
    public static final Integer ISBN_13_LENGTH = 13;
    /**
     * Returns ISBN-13 with correct check digit. Expected to be used to convert ISBN-10
     *
     * @isbn must be at least 978+9 digits from isbn-10
     * @return ISBN-13, all 13 digits
     */
    static public String fixIsbn13checkDigit(String isbn){
        Integer even = 0;
        Integer odd =0;
        Integer checkDigit;
        Integer j;
        for(int i=0;i<6;i++){
            j = i << 1;
            even += Character.digit( isbn.charAt(j),10);
            odd += Character.digit(isbn.charAt(j+1), 10);
        }
        checkDigit = (10 - (even+odd*3) % 10) % 10;
        return isbn.substring(0,ISBN_13_LENGTH -1 )+ String.valueOf(checkDigit);
    }

}
