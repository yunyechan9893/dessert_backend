package com.bbangle.bbangle.board.dto;

import com.bbangle.bbangle.board.domain.Board;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public final class BoardResponseDto {

    private final Long boardId;
    private final Long storeId;
    private final String storeName;
    private final String thumbnail;
    private final String title;
    private final int price;
    private Boolean isWished;
    private final Boolean isBundled;
    private final List<String> tags;

    @Builder
    public BoardResponseDto(
        Long boardId,
        Long storeId,
        String storeName,
        String thumbnail,
        String title,
        int price,
        Boolean isWished,
        Boolean isBundled,
        List<String> tags
    ) {
        this.boardId = boardId;
        this.storeId = storeId;
        this.storeName = storeName;
        this.thumbnail = thumbnail;
        this.title = title;
        this.price = price;
        this.isWished = isWished;
        this.isBundled = isBundled;
        this.tags = tags;
    }

    public static BoardResponseDto from(Board board, List<String> tags) {
        boolean isBundled = board.getProductList()
            .size() > 1;

        return BoardResponseDto.builder()
            .boardId(board.getId())
            .storeId(board.getStore()
                .getId())
            .storeName(board.getStore()
                .getName())
            .thumbnail(board.getProfile())
            .title(board.getTitle())
            .price(board.getPrice())
            .isWished(false)
            .isBundled(isBundled)
            .tags(tags)
            .build();
    }

    public static BoardResponseDto inFolder(Board board, List<String> tags) {
        boolean isBundled = board.getProductList()
            .size() > 1;

        return BoardResponseDto.builder()
            .boardId(board.getId())
            .storeId(board.getStore()
                .getId())
            .storeName(board.getStore()
                .getName())
            .thumbnail(board.getProfile())
            .title(board.getTitle())
            .price(board.getPrice())
            .isWished(true)
            .isBundled(isBundled)
            .tags(tags)
            .build();
    }

    public void updateLike(boolean status){
        this.isWished = status;
    }

    public Long boardId() {
        return boardId;
    }

    public Long storeId() {
        return storeId;
    }

    public String storeName() {
        return storeName;
    }

    public String thumbnail() {
        return thumbnail;
    }

    public String title() {
        return title;
    }

    public int price() {
        return price;
    }

    public Boolean isWished() {
        return isWished;
    }

    public Boolean isBundled() {
        return isBundled;
    }

    public List<String> tags() {
        return tags;
    }


}
