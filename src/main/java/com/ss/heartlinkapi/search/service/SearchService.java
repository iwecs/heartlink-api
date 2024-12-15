package com.ss.heartlinkapi.search.service;

import com.ss.heartlinkapi.elasticSearch.service.DeepLService;
import com.ss.heartlinkapi.elasticSearch.service.ElasticService;
import com.ss.heartlinkapi.elasticSearch.service.Language;
import com.ss.heartlinkapi.follow.entity.FollowEntity;
import com.ss.heartlinkapi.follow.repository.FollowRepository;
import com.ss.heartlinkapi.linktag.entity.LinkTagEntity;
import com.ss.heartlinkapi.linktag.repository.LinkTagRepository;
import com.ss.heartlinkapi.login.dto.CustomUserDetails;
import com.ss.heartlinkapi.post.dto.PostFileDTO;
import com.ss.heartlinkapi.post.dto.PostSearchDTO;
import com.ss.heartlinkapi.post.entity.PostEntity;
import com.ss.heartlinkapi.post.entity.PostFileEntity;
import com.ss.heartlinkapi.post.repository.PostFileRepository;
import com.ss.heartlinkapi.post.repository.PostRepository;
import com.ss.heartlinkapi.profile.service.ProfileService;
import com.ss.heartlinkapi.search.entity.SearchHistoryEntity;
import com.ss.heartlinkapi.search.repository.SearchRepository;
import com.ss.heartlinkapi.user.entity.ProfileEntity;
import com.ss.heartlinkapi.user.entity.Role;
import com.ss.heartlinkapi.user.entity.UserEntity;
import com.ss.heartlinkapi.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LinkTagRepository linkTagRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private SearchRepository searchRepository;

    @Autowired
    private ElasticService elasticService;

    @Autowired
    private DeepLService deepLService;

    @Autowired
    private PostFileRepository postFileRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private ProfileService profileService;

    // 유저 아이디 검색
    @Transactional
    public UserEntity searchByUserId(String keyword, Long userId) {
        keyword = keyword.trim().substring(1);

        UserEntity findUser = userRepository.findByLoginId(keyword);
        UserEntity user = userRepository.findById(userId).orElse(null);

        if(user == null || findUser == null) {
            return null;
        }

        List<SearchHistoryEntity> searchHistoryList = searchRepository.findByKeywordAndTypeAndUserId(keyword, "tag", user.getUserId());
        SearchHistoryEntity elasticEntity;
        if(searchHistoryList != null && searchHistoryList.size() > 0) {
            elasticEntity = searchHistoryList.get(0);
            elasticEntity.setUpdatedAt(LocalDateTime.now());
            searchRepository.save(elasticEntity);
        } else {
            elasticEntity = new SearchHistoryEntity();
            elasticEntity.setUserId(user);
            elasticEntity.setKeyword(user.getLoginId());
            elasticEntity.setType("id");
            elasticEntity.setCreatedAt(LocalDateTime.now());
            SearchHistoryEntity result = searchRepository.save(elasticEntity);
            System.out.println("Result : " + result);
        }
        return findUser;
    }

    // 태그명으로 태그 검색
    public LinkTagEntity searchByTag(String keyword, Long userId) {
        keyword = keyword.trim().substring(1);

        LinkTagEntity findTag = linkTagRepository.findAllByKeyword(keyword);
        UserEntity user = userRepository.findById(userId).orElse(null);

        if(user == null || findTag == null) {
            return null;
        }

        List<SearchHistoryEntity> searchHistoryList = searchRepository.findByKeywordAndTypeAndUserId(keyword, "tag", user.getUserId());
        SearchHistoryEntity elasticEntity;

        if(searchHistoryList != null && searchHistoryList.size() > 0) {
            elasticEntity = searchHistoryList.get(0);
            elasticEntity.setUpdatedAt(LocalDateTime.now());
            searchRepository.save(elasticEntity);
        } else {
            elasticEntity = new SearchHistoryEntity();
            elasticEntity.setUserId(user);
            elasticEntity.setKeyword(keyword);
            elasticEntity.setType("tag");
            elasticEntity.setCreatedAt(LocalDateTime.now());
            SearchHistoryEntity result = searchRepository.save(elasticEntity);
        }
        return findTag;
    }

    // 키워드로 게시글 검색
    @Transactional
    public List<PostEntity> searchByPost(String keyword, Long userId) {
        keyword = keyword.trim();
        List<PostEntity> findPost = postRepository.findAllByContentIgnoreCaseContaining(keyword);
        UserEntity user = userRepository.findById(userId).orElse(null);

        if(user == null || findPost == null) {
            return Collections.emptyList();
        }

        List<SearchHistoryEntity> searchHistoryList = searchRepository.findByKeywordAndTypeAndUserId(keyword, "content", user.getUserId());
        SearchHistoryEntity elasticEntity = new SearchHistoryEntity();
        SearchHistoryEntity searchHistory;
        String deepLResult;

        if(searchHistoryList != null && searchHistoryList.size() > 0) {
            searchHistory = searchHistoryList.get(0);
            searchHistory.setUpdatedAt(LocalDateTime.now());
            searchRepository.save(searchHistory);
            deepLResult = deepLService.translate(searchHistory.getKeyword(), Language.KO, Language.EN);
        } else {
            searchHistory = new SearchHistoryEntity();
            searchHistory.setUserId(user);
            searchHistory.setKeyword(keyword);
            searchHistory.setType("content");
            searchHistory.setCreatedAt(LocalDateTime.now());
            SearchHistoryEntity result = searchRepository.save(searchHistory);
            deepLResult = deepLService.translate(searchHistory.getKeyword(), Language.KO, Language.EN);
        }

        // Elastic용 entity
        elasticEntity.setKeyword(deepLResult);
        elasticEntity.setType(searchHistory.getType());
        elasticEntity.setCreatedAt(searchHistory.getUpdatedAt()==null?searchHistory.getCreatedAt():searchHistory.getUpdatedAt());
        elasticEntity.setUpdatedAt(null);
        elasticEntity.setSearchHistoryId(searchHistory.getSearchHistoryId());
        elasticEntity.setUserId(user);
        elasticService.addOrUpdateHistory(searchHistory);
        return findPost;
    }

    // 유저 아이디로 검색기록 가져오기
    public List<Map<String, Object>> findHistoryByUserId(Long userId) {
       UserEntity user = userRepository.findById(userId).orElse(null);
       List<SearchHistoryEntity> history = searchRepository.findByUserId(user);
       if(history.isEmpty()) {
           return null;
       }
       List<Map<String, Object>> historyList = new ArrayList<>();
       for(int i=0; i<history.size(); i++) {
           Map<String, Object> map = new HashMap<>();
           map.put("type", history.get(i).getType());
           map.put("keyword", history.get(i).getKeyword());
           if(history.get(i).getUpdatedAt() != null) {
               map.put("date", history.get(i).getUpdatedAt());
           } else {
               map.put("date", history.get(i).getCreatedAt());
           }
           historyList.add(map);
       }

       return historyList;
    }

    // 검색창 옆에 띄울 게시글 목록 가져오기
    // 좋아요 많은 순+검색기록 관련 순으로 섞고 나서 연관없는 게시글 최근순으로 가져오기
    public Map<String, Object> getPost(CustomUserDetails user, Integer cursor, int limit) {
        List<PostEntity> manyLikePostList = postRepository.findAllByOrderByLikeCountDesc(); // 좋아요 많은 순으로 게시글 목록 조회
        List<SearchHistoryEntity> searchHistoryList = searchRepository.findByUserId(user.getUserEntity()); // 유저의 검색기록 리스트 조회

        List<PostEntity> searchPostList = new ArrayList<>(); // 검색기록 키워드가 포함된 피드 목록 생성

        for(SearchHistoryEntity searchHistory : searchHistoryList) { // 검색기록 리스트 순회

            if(searchHistory.getType().equals("content")) { // 만약 검색기록의 타입이 content일 때
                // 해당 키워드로 게시글 리스트 모두 조회
                List<PostEntity> keywordFindPostList = postRepository.findAllByContentIgnoreCaseContaining(searchHistory.getKeyword());
                for(PostEntity post : keywordFindPostList) { // 게시글을 검색기록 키워드가 포함된 목록에 저장

                    searchPostList.add(post);
                }
            }
        }

        List<PostEntity> mixPostList = mixPostList(manyLikePostList, searchPostList);
        List<Map<String, Object>> postList = new ArrayList<>();

        if(cursor == null) {
            cursor = 0;
        }

        Integer nextCursor = (cursor + limit < mixPostList.size()) ? cursor + limit : null;

        if (cursor >= mixPostList.size()) {
            cursor = mixPostList.size() - limit;
            if (cursor < 0) cursor = 0;
        }

        int endIndex = (nextCursor != null) ? Math.min(nextCursor, mixPostList.size()) : mixPostList.size();
//        무한스크롤 보류
//        List<PostEntity> sliceData = mixPostList.subList(cursor, endIndex);
        List<PostEntity> sliceData = mixPostList.subList(0, mixPostList.size());
        List<PostSearchDTO> postDTOList = new ArrayList<>();

        for(PostEntity post : sliceData) {
            PostSearchDTO dto = new PostSearchDTO();
            List<PostFileEntity> file = postFileRepository.findByPostId(post.getPostId());
            PostFileEntity fileEntity;
            if(file != null && file.size() > 0) {
                fileEntity = postFileRepository.findByPostId(post.getPostId()).get(0);
                dto.setFileUrl(fileEntity.getFileUrl());
            } else {
                fileEntity = null;
                dto.setFileUrl(null);
            }
            dto.setPostId(post.getPostId());
            postDTOList.add(dto);
        }
        Map<String, Object> postData = new HashMap<>();

        postData.put("nextCursor", nextCursor);
        postData.put("data", postDTOList);
        postData.put("hasNext", nextCursor != null && nextCursor < mixPostList.size());

        return postData;
    }

    // 게시글 섞기 (좋아요 많은 순 + 검색기록에 따른 게시글 리스트)
    private List<PostEntity> mixPostList(List<PostEntity> manyLikePostList, List<PostEntity> searchPostList) {
        if(searchPostList.isEmpty()) { // 검색기록에 따른 피드 내용이 없을 경우
            return manyLikePostList;
        }

        Set<PostEntity> mergedSet = new LinkedHashSet<>(searchPostList);

        for (PostEntity post : manyLikePostList) {
            mergedSet.add(post); // 검색기록 순의 글 목록에 좋아요 순 글 목록을 중복없이 합치기
        }

        return new ArrayList<>(mergedSet);
    }

    // 언급 시 아이디 리스트 조회(팔로우 우선 순)
    public List<Map<String, Object>> mentionIdList(UserEntity user) {
        // 유저가 팔로우한 회원 리스트
        List<FollowEntity> followList = followRepository.findByFollowerUserIdAndStatusIsTrue(user.getUserId());

        // 팔로우한 회원 ID를 Set에 저장하여 중복 확인
        Set<Long> followedUserIds = followList.stream()
                .map(f -> f.getFollowing().getUserId())
                .collect(Collectors.toSet());

        // 팔로우 우선 리스트에 팔로우한 회원들 추가
        Set<UserEntity> followFirstList = new LinkedHashSet<>(); // 순서를 유지하기 위해 LinkedHashSet 사용
        for (FollowEntity follow : followList) {
            followFirstList.add(follow.getFollowing());
        }

        // 롤이 싱글이거나 커플인 모든 유저 리스트
        List<UserEntity> userList = userRepository.findByRoleIn(Arrays.asList(Role.ROLE_COUPLE, Role.ROLE_SINGLE));

        // 팔로우하지 않은 유저들만 추가
        for (UserEntity userEntity : userList) {
            if (!followedUserIds.contains(userEntity.getUserId()) &&
                    !user.getUserId().equals(userEntity.getUserId())) {
                followFirstList.add(userEntity);
            }
        }

        // 리스트<맵> 형태로 변환
        List<Map<String, Object>> followMapList = new ArrayList<>();
        for(UserEntity followUser : followFirstList) {
            Map<String, Object> followMap = new HashMap<>();
            ProfileEntity profile = profileService.findByUserEntity(followUser);
            followMap.put("userId", followUser.getUserId());
            followMap.put("loginId", followUser.getLoginId());
            followMap.put("profileUrl", profile.getProfile_img());
            followMapList.add(followMap);
        }
        return followMapList;
    }

    public List<Map<String, Object>> findGroupByPostId() {
        List<PostEntity> postList = postRepository.findAll();
        List<Map<String, Object>> postFileList = new ArrayList<>();
        for(PostEntity postEntity : postList) {
            Map<String, Object> postMap = new HashMap<>();
            postMap.put("postId", postEntity.getPostId());
            postMap.put("fileUrl", postFileRepository.findByPostId(postEntity.getPostId()).get(0));
            postFileList.add(postMap);
        }
        return postFileList;
    }
}
