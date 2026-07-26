package com.agentlearning.stage1;

public interface Tool {
    String name();

    String description();

    String execute(String argument) throws Exception;
}
