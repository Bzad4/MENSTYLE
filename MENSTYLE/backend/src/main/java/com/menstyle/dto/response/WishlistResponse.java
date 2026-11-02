package com.menstyle.dto.response;

import java.util.List;

public record WishlistResponse(Long id, List<WishlistItemResponse> items) {}
