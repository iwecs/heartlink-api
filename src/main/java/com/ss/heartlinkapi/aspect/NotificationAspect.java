package com.ss.heartlinkapi.aspect;

import com.ss.heartlinkapi.comment.dto.CommentDTO;
import com.ss.heartlinkapi.follow.entity.FollowEntity;
import com.ss.heartlinkapi.login.dto.CustomUserDetails;
import com.ss.heartlinkapi.notification.dto.NotificationLikeDTO;
import com.ss.heartlinkapi.notification.service.NotificationService;
import com.ss.heartlinkapi.post.repository.PostRepository;
import com.ss.heartlinkapi.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.mapping.Join;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class NotificationAspect {

    private final NotificationService notificationService;
    private final PostRepository postRepository;

//    aop는 매개변수 타입이나 이름이 바뀌면 똑같이 바꿔줘야 한다. 그렇지 않으면 작동 X
    @AfterReturning(value = "execution(* com.ss.heartlinkapi.comment.service.CommentService.writeComment(..)) && args(commentDTO, user)", argNames = "joinPoint,commentDTO,user")
    public void notifyComment(final JoinPoint joinPoint, final CommentDTO commentDTO, final UserEntity user){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        notificationService.notifyComment(authentication.getName(),commentDTO.getPostId(), user.getUserId());
    }

    @AfterReturning("execution(* com.ss.heartlinkapi.follow.repository.FollowRepository.save(..)) && args(followEntity)" )
    public void notifyFollow(final JoinPoint joinPoint, final FollowEntity followEntity){
        if(followEntity.isStatus()){
            notificationService.notifyFollow(followEntity.getFollower().getLoginId(), followEntity.getFollowing().getUserId(), followEntity.getFollower().getUserId());
        }
    }

}
