package com.agentlearning.stage1;

public interface Model {
    ModelDecision decide(String observation) throws Exception;
}
