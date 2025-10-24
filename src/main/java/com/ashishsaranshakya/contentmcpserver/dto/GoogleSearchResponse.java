package com.ashishsaranshakya.contentmcpserver.dto;


import java.util.List;

public record GoogleSearchResponse(
        List<Item> items // The array of search results
) {}
