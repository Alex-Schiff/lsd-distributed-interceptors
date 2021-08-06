package io.lsdconsulting.lsd.distributed.diagram.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Interaction {
    String source;
    String destination;
    String path;
    String httpStatus;
    String httpMethod;
    String createdAt;
    String colour;
    String elapsedTime;
}