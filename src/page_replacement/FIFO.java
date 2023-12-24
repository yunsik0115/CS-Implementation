package page_replacement;

import java.util.*;

public class FIFO {

    static int pageFaults(int pages[], int capacity){

        HashSet<Integer> set = new HashSet<>(capacity); // 내부에 값이 있는지 검증

        Queue<Integer> indexes = new LinkedList<>();
        // FIFO의 특성을 가진 Queue를 통한 Paging

        int page_faults = 0;
        for(int i = 0; i< pages.length; i++){
            if(set.size() < capacity){ // Page Table에 공간이 더 남아있는가?
                if(!set.contains(pages[i])){ // Page Table에 해당 내용이 없는 경우
                    set.add(pages[i]); // 페이지 추가
                    page_faults++; // 폴트 횟수 증가
                    indexes.add(pages[i]); // 페이지 Entry Key를 FIFO Queue에 추가
                    System.out.println("pages[i] 첫 진입 = " + pages[i]);
                }
            }
            else{ // 페이지 테이블에 공간이 남아있지 않은 경우
                if(!set.contains(pages[i])){ // 페이지 테이블에 해당 내용이 없으면
                    int value = indexes.poll(); // 맨 앞에 있는 값을 반환하고 삭제함
                    set.remove(value); // 페이지에 들어있는 값에서 제거
                    set.add(pages[i]); // 새로운 페이지를 추가함
                    indexes.add(pages[i]); // FIFO Queue에 추가된 새로운 페이지 추가
                    page_faults++;

                    System.out.println("swapping value :  " + value + " || pages[i] : " + pages[i]);
                }
            }
        }
        return page_faults;
    }

    public static void main(String[] args) {

        int pages[] = {7, 0, 1, 2, 0, 3, 0, 4,
                2, 3, 0, 3, 2};

        int capacity = 4;

        System.out.println(pageFaults(pages, capacity));


    }
}
