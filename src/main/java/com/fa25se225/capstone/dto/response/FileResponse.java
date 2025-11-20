package com.fa25se225.capstone.dto.response;

public record FileResponse(byte[] bytes, String contentType, String fileName) {}

