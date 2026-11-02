package com.menstyle.entity;
import com.menstyle.entity.enums.AiMessageSender; import jakarta.persistence.*; import lombok.*;
@Getter @Setter @NoArgsConstructor @Entity @Table(name="ai_messages")
public class AiMessage extends BaseEntity { @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="conversation_id",nullable=false) private AiConversation conversation; @Enumerated(EnumType.STRING) @Column(nullable=false,length=10) private AiMessageSender sender; @Column(nullable=false,columnDefinition="nvarchar(max)") private String content; }
