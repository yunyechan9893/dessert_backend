package com.bbangle.bbangle.board.controller;

import com.bbangle.bbangle.config.ranking.BoardLikeInfo;
import com.bbangle.bbangle.config.ranking.ScoreType;
import com.bbangle.bbangle.board.dto.BoardDetailResponseDto;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.common.message.MessageResDto;
import com.bbangle.bbangle.board.service.BoardServiceImpl;
import com.bbangle.bbangle.util.RedisKeyUtil;
import com.bbangle.bbangle.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/boards")
@RequiredArgsConstructor
public class BoardController {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH");
    private final BoardServiceImpl boardService;
    @Qualifier("defaultRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;
    @Autowired
    @Qualifier("boardLikeInfoRedisTemplate")
    private final RedisTemplate<String, Object> boardLikeInfoRedisTemplate;

    @GetMapping
    public ResponseEntity<Slice<BoardResponseDto>> getList(
        @RequestParam(required = false)
        String sort,
        @RequestParam(required = false)
        Boolean glutenFreeTag,
        @RequestParam(required = false)
        Boolean highProteinTag,
        @RequestParam(required = false)
        Boolean sugarFreeTag,
        @RequestParam(required = false)
        Boolean veganTag,
        @RequestParam(required = false)
        Boolean ketogenicTag,
        @RequestParam(required = false)
        String category,
        @RequestParam(required = false)
        Integer minPrice,
        @RequestParam(required = false)
        Integer maxPrice,
        @RequestParam(required = false)
        Boolean orderAvailableToday,
        @PageableDefault
        Pageable pageable
    ) {
        return ResponseEntity.ok(boardService.getBoardList(sort,
            glutenFreeTag,
            highProteinTag,
            sugarFreeTag,
            veganTag,
            ketogenicTag,
            category,
            minPrice,
            maxPrice,
            orderAvailableToday,
            pageable));
    }

    @GetMapping("/folders/{folderId}")
    public ResponseEntity<Slice<BoardResponseDto>> getPostInFolder(
        @RequestParam(required = false)
        String sort,
        @PathVariable
        Long folderId,
        @PageableDefault
        Pageable pageable
    ) {
        Long memberId = SecurityUtils.getMemberId();
        return ResponseEntity.ok(boardService.getPostInFolder(memberId, sort, folderId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardDetailResponseDto> getBoardDetailResponse(
        @PathVariable("id")
        Long boardId
    ) {
        Long memberId = SecurityUtils.getMemberIdWithAnonymous();

        return ResponseEntity.ok().body(
            boardService.getBoardDetailResponse(memberId, boardId)
        );
    }

    @PatchMapping("/{boardId}")
    public ResponseEntity<Void> countView(
        @PathVariable
        Long boardId, HttpServletRequest request
    ) {
        String ipAddress = request.getRemoteAddr();
        String viewCountKey = "VIEW:" + boardId + ":" + ipAddress;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(viewCountKey))) {
            return ResponseEntity.badRequest()
                .build();
        }

        redisTemplate.opsForZSet()
            .incrementScore(RedisKeyUtil.POPULAR_KEY, String.valueOf(boardId), 0.1);
        boardLikeInfoRedisTemplate.opsForList()
            .rightPush(LocalDateTime.now()
                    .format(formatter),
                new BoardLikeInfo(boardId, 0.1, LocalDateTime.now(), ScoreType.VIEW));
        redisTemplate.opsForValue()
            .set(viewCountKey, true, Duration.ofMinutes(3));

        return ResponseEntity.status(HttpStatus.OK)
            .build();
    }

    @PatchMapping("/{boardId}/purchase")
    public ResponseEntity<Void> movePurchasePage(
        @PathVariable
        Long boardId, HttpServletRequest request
    ) {
        String ipAddress = request.getRemoteAddr();
        String purchaseCountKey = "PURCHASE:" + boardId + ":" + ipAddress;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(purchaseCountKey))) {
            return ResponseEntity.badRequest()
                .build();
        }

        redisTemplate.opsForZSet()
            .incrementScore(RedisKeyUtil.POPULAR_KEY, String.valueOf(boardId), 1);
        boardLikeInfoRedisTemplate.opsForList()
            .rightPush(LocalDateTime.now()
                    .format(formatter),
                new BoardLikeInfo(boardId, 1, LocalDateTime.now(), ScoreType.PURCHASE));
        redisTemplate.opsForValue()
            .set(purchaseCountKey, true, Duration.ofMinutes(3));

        return ResponseEntity.status(HttpStatus.OK)
            .build();
    }

    @PatchMapping(value = "/{boardId}/detail", consumes = {"multipart/form-data"})
    public ResponseEntity<Object> putBoardDetailUrl(
        @PathVariable("boardId")
        Long boardId,
        @RequestParam("htmlFile")
        MultipartFile htmlFile
    ) {
        String successMessage = "파일 저장에 성공하셨습니다";
        String failMessage = "파일 저장에 실패하셨습니다";

        if (boardService.saveBoardDetailHtml(boardId, htmlFile)) {
            return ResponseEntity.ok()
                .body(MessageResDto.builder()
                    .message(successMessage)
                    .build()
                );
        }

        // 예상치 못한 에러 발생
        return ResponseEntity.ok()
            .body(MessageResDto.builder()
                .message(failMessage)
                .build());
    }

}

