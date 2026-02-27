//package com.ashishsaranshakya.outputmcpserver.config;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import io.modelcontextprotocol.server.McpServer;
//import io.modelcontextprotocol.server.McpSyncServer;
//import io.modelcontextprotocol.server.transport.WebMvcSseServerTransportProvider;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.EnableWebMvc;
//import org.springframework.web.servlet.function.RouterFunction;
//import org.springframework.web.servlet.function.ServerResponse;
//
//@Configuration
//@EnableWebMvc
//public class McpConfig {
//    private final String MESSAGE_ENDPOINT = "/comms";
//
//    @Bean
//    public WebMvcSseServerTransportProvider webMvcSseServerTransportProvider() {
//        return new WebMvcSseServerTransportProvider(new ObjectMapper(), MESSAGE_ENDPOINT);
//    }
//
//    @Bean
//    public RouterFunction<ServerResponse> routerFunction(WebMvcSseServerTransportProvider transportProvider) {
//        return transportProvider.getRouterFunction();
//    }
//
//    @Bean
//    public McpSyncServer mcpSyncServer(WebMvcSseServerTransportProvider transportProvider) {
//        return McpServer.sync(transportProvider)
//                .build();
//    }
//}
