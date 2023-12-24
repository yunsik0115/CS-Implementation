package page_replacement;

import java.io.*;
import java.util.*;
import static java.lang.System.exit;

public class LRU {

    static class PageTable{
        int[] pages;

        public PageTable(int num_pages) {
            this.pages = new int[num_pages];
        }
    }

    static int ADDR_SIZE = 27; // 전체 주소 공간의 크기 2^27
    static int NUM_FRAMES = 64; //주어진 시스템은 64개의 물리 프레임이 있음

    static int page_size = -1; // 한 페이지의 크기
    static int num_pages = -1; // 각 프로세스의 페이지 갯수


    static int[] physicalMemory = new int[NUM_FRAMES];

    static int pageFaults = 0;

    static int frameFull = -1;      //모든 물리프레임이 사용된 경우 1, 아닌 경우 -1

    static int frameNumber = -1; //페이지 폴트 시 사용할 물리 페이지 번호(index)

    // 사용하지 않는 프레임을 순차적으로 할당함
    // (p.s. 실제 시스템은 이런식으로 순차할당하지 않습니다.)
    static int nextFrameNumber = 0;

    static int victimPage = -1; //교체될(evictee) 페이지 번호

    static int defaultVictim = 0;// 샘플 교체정책에 사용되는 변수

    static void calculatePageInfo(int page_bits){
        page_size = 1 << page_bits; // 2^page bits
        num_pages = (1 << (ADDR_SIZE - page_bits)); // 2^(27-페이지 비트)
        // 운영체제에서 Page 엔트리 구조를 보면 이런 구조로 되어있음.
    }

    static int getPageNumber(int virtualAddress){
        return virtualAddress / page_size;
        // Virtual Address Space를 Page Size로 나눈 값 - 엔트리 개수
    }

    static int doPageReplacement(char policy, PageTable pageTable){

        switch (policy) {
            case 'd': //샘플: 기본(default) 교체 정책
            case 'D': //순차교체: 교체될 페이지 엔트리 번호를 순차적으로 증가시킴
                while (true) {
                    //유효한(물리프레임에 저장된) 페이지를 순차적으로 찾음
                    if (pageTable.pages[defaultVictim] != -1) {
                        break;
                    }
                    defaultVictim = (defaultVictim + 1) % num_pages;
                }
                victimPage = defaultVictim;
                break;
            case 'r':
            case 'R': //TODO-4-1: 교체 페이지를 임의(random)로 선정
                Random rand = new Random();
                while(true) {
                    if (pageTable.pages[defaultVictim] != -1) {
                        break;
                    }
                    defaultVictim = rand.nextInt(0, num_pages - 1);
                }
                victimPage = defaultVictim;
                break;
            case 'a':
            case 'A':
                //(Option)TODO-4-2: 기타 교체 정책 구현해보기
                //FIFO, LRU, 나만의 정책 등, 다른 교체 알고리즘울 조사 후 구현
                //1개 이상의 교체 정책 추가로 구현 가능.
                //다수의 알고리즘 개발 시, 알고리즘 선택 변수(char policy)는 a,b,c,...순으로 구현
                break;

            default:
                System.out.println("ERROR: 정의되지 않은 페이지 교체 정책\n");
                exit(1);
                break;
        }

        frameNumber = pageTable.pages[victimPage]; //교체된 페이지를 통해 사용 가능해진 물리 프레임 번호
        pageTable.pages[victimPage] = -1;  //교체된 페이지는 더 이상 물리 메모리에 있지 않음을 기록

        return frameNumber;
    }

    // 페이지 폴트 처리
    static void handlePageFault(int pageNumber, char policy, PageTable pageTable) {

        //물리 프레임에 여유가 있는 경우
        if (frameFull == -1) {
            frameNumber = nextFrameNumber++;
            //모든 물리 프레임이 사용된 경우, 이를 마크함
            if(nextFrameNumber == NUM_FRAMES)
                frameFull = 1;
        }
        //모든 물리 프레임이 사용중. 기존 페이지를 교체해야 함
        else {
            frameNumber = doPageReplacement(policy, pageTable);
        }

        // 페이지 테이블 업데이트
        pageTable.pages[pageNumber] = frameNumber;

        System.out.println("페이지 폴트 발생: 페이지 " + pageNumber + "를 프레임 "+ frameNumber +"로 로드\n");
        pageFaults++;
    }

    public static void main(String[] args) throws IOException {


        System.out.println("please input the parameter!");
        Scanner sc = new Scanner(System.in);

        int page_bits = sc.nextInt();  //입력받은 페이지 오프셋(offset) 크기

        System.out.println("please input the parameter! Page Strategy?");
        char policy = sc.next().charAt(0);       //입력받은 페이지 교체 정책

        calculatePageInfo(page_bits);

        System.out.println
                ("입력된 페이지 별 크기: "+ page_size +"Bytes\n프로세스의 페이지 개수: " + num_pages + "개\n페이지 교체 알고리즘:"+ policy);


        // 페이지 테이블 할당 및 초기화
        PageTable pageTable = new PageTable(num_pages);
        // 페이지 테이블 초기화
        for (int i = 0; i < num_pages; i++)
            pageTable.pages[i] = -1;
        // 물리 프레임 초기화
        for (int i = 0; i < NUM_FRAMES; i++)
            physicalMemory[i] = 0;


        // 파일 읽기
        String filename = "/Users/yunsik/Desktop/CS-Implementation/src/page_replacement/input.txt";
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        // 파일 내 데이터: 가상 메모리 주소
        // 모든 메모리 주소에 대해서
        int lineNumber = 0;
        while ((line = reader.readLine()) != null) {

            int address = Integer.parseInt(line);
            //System.out.print("address = " + address + " ");

            // 가상 주소에서 페이지 번호(pageNumber)를 얻음
            int pageNumber = getPageNumber(address);
            //System.out.println("pageNumber = " + pageNumber);

            // pageTable 함수는 페이지 폴트 시 -1 값을 반환함
            int frameNumber = pageTable.pages[pageNumber];
            if (frameNumber == -1) { //page fault
                handlePageFault(pageNumber, policy, pageTable); //페이지 폴트 핸들러
                frameNumber = pageTable.pages[pageNumber];
            }

            //해당 물리 프레임을 접근하고 접근 횟수를 셈
            physicalMemory[frameNumber]++;

            lineNumber++;
            // sleep(1000); //매 페이지 접근 처리 후 0.001초간 멈춤
            //이 delay는 프로세스 수행 중, signal발생 처리과정을 확인하기 위함이며,
            //구현을 수행하는 도중에는 주석처리하여, 빠르게 결과확인을 하기 바랍니다.
        }

        reader.close();
        //free(pageTable);

        // 작업 수행 완료. Alarm 시그널을 기다림.
        //processDone = 1;
        //printf("프로세스가 완료되었습니다. 종료 신호를 기다리는 중...\n");
        //while (contFlag == 0){};


        // 결과 출력
        System.out.println("\n---물리 프레임 별 접근 횟수----\n");
        for (int i = 0; i < NUM_FRAMES; i++) {
            System.out.println(i + " frame:" + physicalMemory[i]);
        }
        System.out.println("----------\n페이지 폴트 횟수: " + pageFaults);

    }




}