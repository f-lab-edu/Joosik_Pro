package com.joopro.Joosik_Pro;

import com.joopro.Joosik_Pro.domain.*;
import com.joopro.Joosik_Pro.domain.Post.Post;
import com.joopro.Joosik_Pro.domain.Post.SingleStockPost;
import com.joopro.Joosik_Pro.domain.Post.VsStockPost;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
public class DataLoader implements CommandLineRunner {

    @PersistenceContext
    private EntityManager em;

    private final Random random = new Random();

    private static final int NUM_MEMBERS = 500;
    private static final int NUM_STOCKS = 200;
    private static final int NUM_STOCK_MEMBERSHIPS_PER_MEMBER_MAX = 10; // 멤버당 최대 구독 주식 수
    private static final int NUM_SINGLE_STOCK_POSTS = 800;
    private static final int NUM_VS_STOCK_POSTS = 400;
    private static final int NUM_OPINIONS_PER_POST_MAX = 20; // 게시글당 최대 댓글 수
    private static final int OPINION_MAX_DEPTH = 2; // 댓글 최대 깊이 (0: 원댓글, 1: 대댓글, 2: 대대댓글)

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("DataLoader 시작: 데이터 초기화 작업을 수행합니다...");

        Long memberCount = em.createQuery("SELECT COUNT(m) FROM Member m", Long.class).getSingleResult();

        // 1. Member 생성
        List<Member> members = generateMembers(NUM_MEMBERS);
        members.forEach(em::persist);
        em.flush(); // ID 할당을 위해 flush
        System.out.println(members.size() + "명의 Member 생성 완료.");

        // 2. Stock, DomesticStock, ForeignStock 생성
        List<Stock> stocks = generateStocks(NUM_STOCKS);
        stocks.forEach(stock -> {
            em.persist(stock);
            // Stock 저장 후 DomesticStock 또는 ForeignStock 생성 및 저장
            if (random.nextBoolean()) { // 50% 확률로 DomesticStock
                DomesticStock domesticStock = generateDomesticStock(stock);
                em.persist(domesticStock);
            } else { // 50% 확률로 ForeignStock
                ForeignStock foreignStock = generateForeignStock(stock);
                em.persist(foreignStock);
            }
        });
        em.flush(); // ID 할당을 위해 flush
        System.out.println(stocks.size() + "개의 Stock (및 Domestic/Foreign) 생성 완료.");


        // 3. StockMembership 생성
        List<StockMembership> stockMemberships = generateStockMemberships(members, stocks);
        stockMemberships.forEach(em::persist);
        em.flush();
        System.out.println(stockMemberships.size() + "개의 StockMembership 생성 완료.");


        // 4. Post (SingleStockPost, VsStockPost) 생성
        List<Post> allPosts = new ArrayList<>();

        List<SingleStockPost> singleStockPosts = generateSingleStockPosts(members, stocks, NUM_SINGLE_STOCK_POSTS);
        singleStockPosts.forEach(em::persist);
        allPosts.addAll(singleStockPosts);
        em.flush();
        System.out.println(singleStockPosts.size() + "개의 SingleStockPost 생성 완료.");

        List<VsStockPost> vsStockPosts = generateVsStockPosts(members, stocks, NUM_VS_STOCK_POSTS);
        vsStockPosts.forEach(em::persist);
        allPosts.addAll(vsStockPosts);
        em.flush();
        System.out.println(vsStockPosts.size() + "개의 VsStockPost 생성 완료.");


        // 5. Opinion 생성 (계층 구조 포함)
        List<Opinion> opinions = generateOpinionsRecursive(members, allPosts, 0, OPINION_MAX_DEPTH, NUM_OPINIONS_PER_POST_MAX);
        opinions.forEach(em::persist);
        em.flush();
        System.out.println(opinions.size() + "개의 Opinion 생성 완료.");


        System.out.println("DataLoader 작업 완료.");
    }

    private List<Member> generateMembers(int count) {
        List<Member> members = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String name = "사용자" + i + "_" + randomString(4);
            String email = "user" + i + "_" + randomString(6) + "@joopro.com";
            Member member = Member.builder()
                    .name(name)
                    .password("password123")
                    .email(email)
                    .build();
            members.add(member);
        }
        return members;
    }

    private List<Stock> generateStocks(int count) {
        List<Stock> stocks = new ArrayList<>();
        String[] sectors = {"기술", "금융", "소비재", "헬스케어", "에너지", "산업재", "부동산", "유틸리티"};
        for (int i = 0; i < count; i++) {
            String companyName = "기업" + i + " (" + randomString(3) + ")";
            String ticker = randomString(4).toUpperCase();
            String sector = sectors[random.nextInt(sectors.length)];
            Stock stock = Stock.builder()
                    .companyName(companyName)
                    .ticker(ticker)
                    .sector(sector)
                    .build();
            stocks.add(stock);
        }
        return stocks;
    }

    private DomesticStock generateDomesticStock(Stock stock) {
        double currentPrice = randomDouble(10000, 500000);
        double highPrice = currentPrice * (1 + random.nextDouble() * 0.1); // 현재가보다 최대 10% 높게
        double lowPrice = currentPrice * (1 - random.nextDouble() * 0.1);  // 현재가보다 최대 10% 낮게
        return DomesticStock.builder()
                .stock(stock)
                .현재가(currentPrice)
                .최고가(highPrice)
                .최저가(lowPrice)
                .상한가(currentPrice * 1.3)
                .하한가(currentPrice * 0.7)
                .from250HighPrice(highPrice * (1 + random.nextDouble() * 0.2))
                .from250LowPrice(lowPrice * (1 - random.nextDouble() * 0.2))
                .fromYearHighPrice(highPrice * (1 + random.nextDouble() * 0.25))
                .fromYearLowPrice(lowPrice * (1 - random.nextDouble() * 0.25))
                .from52wHighPrice(highPrice * (1 + random.nextDouble() * 0.22))
                .from52wLowPrice(lowPrice * (1 - random.nextDouble() * 0.22))
                .per(randomDouble(5, 30))
                .pbr(randomDouble(0.5, 5))
                .eps(randomDouble(1000, 10000))
                .build();
    }

    private ForeignStock generateForeignStock(Stock stock) {
        return ForeignStock.builder()
                .stock(stock)
                .현재가(randomDouble(10, 500)) // 달러 기준
                .전일종가(randomDouble(10, 500))
                .거래량(random.nextInt(100000, 10000000))
                .build();
    }


    private List<StockMembership> generateStockMemberships(List<Member> members, List<Stock> stocks) {
        List<StockMembership> memberships = new ArrayList<>();
        if (members.isEmpty() || stocks.isEmpty()) return memberships;

        for (Member member : members) {
            int numSubscriptions = random.nextInt(NUM_STOCK_MEMBERSHIPS_PER_MEMBER_MAX + 1);
            // 중복되지 않게 주식 선택
            List<Stock> shuffledStocks = new ArrayList<>(stocks);
            java.util.Collections.shuffle(shuffledStocks, random);

            for (int i = 0; i < numSubscriptions && i < shuffledStocks.size(); i++) {
                Stock stock = shuffledStocks.get(i);
                StockMembership membership = StockMembership.createStockMemberShip(member, stock);
                if (random.nextDouble() < 0.1) { // 10% 확률로 비활성
                    membership.cancel();
                }
                memberships.add(membership);
            }
        }
        return memberships;
    }

    private List<SingleStockPost> generateSingleStockPosts(List<Member> members, List<Stock> stocks, int count) {
        List<SingleStockPost> posts = new ArrayList<>();
        if (members.isEmpty() || stocks.isEmpty()) return posts;

        for (int i = 0; i < count; i++) {
            Member author = getRandomElement(members);
            Stock stock = getRandomElement(stocks);
            String content = "이 주식(" + stock.getCompanyName() + ")에 대한 생각: " + randomLoremIpsum(1, 3);

            SingleStockPost post = SingleStockPost.makeSingleStockPost(content, author, stock);
            post.setViewCount(random.nextLong(1000)); // 조회수 랜덤 설정
            posts.add(post);
        }
        posts.forEach(p -> p.getStock().incrementArticleNumber());
        return posts;
    }

    private List<VsStockPost> generateVsStockPosts(List<Member> members, List<Stock> stocks, int count) {
        List<VsStockPost> posts = new ArrayList<>();
        if (members.isEmpty() || stocks.size() < 2) return posts;

        for (int i = 0; i < count; i++) {
            Member author = getRandomElement(members);
            Stock stock1 = getRandomElement(stocks);
            Stock stock2;
            do {
                stock2 = getRandomElement(stocks);
            } while (stock1.getId() != null && stock1.getId().equals(stock2.getId()) || stock1 == stock2); // 동일 주식 방지 (ID가 아직 없을 수 있으므로 객체 비교도 포함)

            String content = stock1.getCompanyName() + " vs " + stock2.getCompanyName() + ": " + randomLoremIpsum(2, 4);
            VsStockPost post = VsStockPost.makeVsStockPost(content, author, stock1, stock2);
            post.setViewCount(random.nextLong(1500));
            posts.add(post);
        }
        return posts;
    }


    private List<Opinion> generateOpinionsRecursive(List<Member> members, List<Post> posts, int currentDepth, int maxDepth, int opinionsPerEntity) {
        List<Opinion> allOpinions = new ArrayList<>();
        if (members.isEmpty() || posts.isEmpty() || currentDepth > maxDepth) {
            return allOpinions;
        }

        for (Post post : posts) {
            int numberOfOpinions = random.nextInt(opinionsPerEntity + 1);
            for (int i = 0; i < numberOfOpinions; i++) {
                Member commenter = getRandomElement(members);
                String commentText = "댓글 (" + (currentDepth > 0 ? "대댓글" : "원댓글") + "): " + randomLoremIpsum(1, 2);

                Opinion opinion = Opinion.createOpinion(commentText, post, commenter);

                int targetLikes = random.nextInt(100);  // 0부터 99 사이의 난수
                int targetDislikes = random.nextInt(20); // 0부터 19 사이의 난수
                for (int j = 0; j < targetLikes; j++) {
                    opinion.press_like();
                }
                for (int j = 0; j < targetDislikes; j++) {
                    opinion.press_dislike();
                }

                if (random.nextDouble() < 0.05) { // 5% 확률로 삭제된 댓글
                    opinion.Delete();
                }

                allOpinions.add(opinion);

                if (currentDepth < maxDepth) {
                    allOpinions.addAll(generateChildOpinions(members, opinion, currentDepth + 1, maxDepth, random.nextInt(opinionsPerEntity / 2 + 1)));
                }
            }
        }
        return allOpinions;
    }

    private List<Opinion> generateChildOpinions(List<Member> members, Opinion parentOpinion, int currentDepth, int maxDepth, int numChildren) {
        List<Opinion> childOpinions = new ArrayList<>();
        if (currentDepth > maxDepth || parentOpinion.isDeleted()) {
            return childOpinions;
        }

        for (int i = 0; i < numChildren; i++) {
            Member commenter = getRandomElement(members);
            String commentText = "대댓글 (depth " + currentDepth + "): " + randomLoremIpsum(1,1);
            Opinion child = Opinion.createOpinion(commentText, parentOpinion.getPost(), commenter);
            child.setParentOpinion(parentOpinion);

            int targetLikes = random.nextInt(100);  // 0부터 99 사이의 난수
            int targetDislikes = random.nextInt(20); // 0부터 19 사이의 난수
            for (int j = 0; j < targetLikes; j++) {
                child.press_like();
            }
            for (int j = 0; j < targetDislikes; j++) {
                child.press_dislike();
            }

            if (random.nextDouble() < 0.05) { // 5% 확률로 삭제된 댓글
                child.Delete();
            }
            childOpinions.add(child);

            if (currentDepth < maxDepth) {
                childOpinions.addAll(generateChildOpinions(members, child, currentDepth + 1, maxDepth, random.nextInt(numChildren / 2 + 1)));
            }

        }
        return childOpinions;
    }


    // --- Helper Methods ---
    private String randomString(int length) {
        return UUID.randomUUID().toString().replace("-", "").substring(0, Math.min(length, 32));
    }

    private double randomDouble(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }

    private <T> T getRandomElement(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(random.nextInt(list.size()));
    }

    private String randomLoremIpsum(int minParagraphs, int maxParagraphs) {
        String[] loremWords = (
                "apple banana cat dog house car tree book computer data " +
                        "table chair window door floor ceiling garden park city street " +
                        "river mountain ocean sky cloud sun moon star time idea " +
                        "information knowledge power money love happiness peace freedom " +
                        "art music science technology history future present past moment " +
                        "system process project service product customer user team member " +
                        "company market industry strategy goal plan result progress " +
                        "change growth development innovation solution problem challenge opportunity " +
                        "run walk talk speak listen see look watch think learn " +
                        "study work play create build design develop test deploy " +
                        "manage lead follow help support share like love hate " +
                        "begin start end finish continue stop change grow improve " +
                        "achieve succeed fail try explore discover understand explain " +
                        "good bad happy sad big small new old young high low " +
                        "long short fast slow bright dark light heavy simple complex " +
                        "easy difficult important interesting beautiful wonderful amazing " +
                        "strong weak brave smart kind gentle calm clear " +
                        "active creative productive efficient effective reliable secure " +
                        "quickly slowly carefully easily happily sadly well badly " +
                        "always often sometimes never usually generally " +
                        "here there now then soon later early today tomorrow yesterday " +
                        "very really quite too also just only even " +
                        "software hardware network server client database API cloud " +
                        "interface algorithm security performance scalability feature release "
        ).split(" ");

        if (loremWords.length == 0) {
            return ""; // 사용할 단어가 없으면 빈 문자열 반환
        }

        // 단락 수 결정 (minParagraphs와 maxParagraphs 사이)
        // maxParagraphs가 minParagraphs보다 작거나 같을 경우 minParagraphs로 고정, 아닐 경우 범위 내 랜덤
        int numParagraphs;
        if (minParagraphs <= 0 && maxParagraphs <= 0) { // 둘 다 0 이하이면 0개 단락
            numParagraphs = 0;
        } else if (minParagraphs > maxParagraphs) { // 최소값이 최대값보다 크면 최소값으로 (혹은 오류 처리)
            numParagraphs = minParagraphs;
        } else {
            // this.random.nextInt(N)은 0부터 N-1까지 반환
            // 따라서 [min, max] 범위는 this.random.nextInt(max - min + 1) + min
            numParagraphs = this.random.nextInt(maxParagraphs - Math.max(0, minParagraphs) + 1) + Math.max(0, minParagraphs);
        }

        if (numParagraphs <= 0) {
            return ""; // 생성할 단락이 없으면 빈 문자열 반환
        }

        StringBuilder resultTextBuilder = new StringBuilder();

        for (int p = 0; p < numParagraphs; p++) {
            // 단락당 문장 수 결정 (예: 2~4 문장)
            int numSentences = this.random.nextInt(3) + 2; // 0,1,2 -> 2,3,4

            for (int s = 0; s < numSentences; s++) {
                // 문장당 단어 수 결정 (예: 5~14 단어)
                int numWordsInSentence = this.random.nextInt(10) + 5; // 0..9 -> 5..14
                StringBuilder sentenceBuilder = new StringBuilder();
                for (int w = 0; w < numWordsInSentence; w++) {
                    sentenceBuilder.append(loremWords[this.random.nextInt(loremWords.length)]);
                    if (w < numWordsInSentence - 1) { // 마지막 단어가 아니면 공백 추가
                        sentenceBuilder.append(" ");
                    }
                }
                sentenceBuilder.append("."); // 문장 끝에 마침표
                resultTextBuilder.append(sentenceBuilder);
            }

            if (p < numParagraphs - 1) { // 마지막 단락이 아니면 줄바꿈 추가 (단락 구분)
                resultTextBuilder.append("\n\n"); // 두 번 줄바꿈으로 단락 간 간격 확보
            }
        }
        return resultTextBuilder.toString();
    }

}