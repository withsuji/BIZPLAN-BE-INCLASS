package vibe.bizplan.bizplan_be_inclass.summary.entity;

import jakarta.persistence.*;
import lombok.*;
import vibe.bizplan.bizplan_be_inclass.calendar.entity.CalendarEvent;
import vibe.bizplan.bizplan_be_inclass.common.entity.BaseEntity;

/**
 * Summary note entity for meeting summaries.
 * REQ-FUNC-011: Automatic meeting summary generation
 */
@Entity
@Table(name = "summary_notes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SummaryNote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false, unique = true)
    private CalendarEvent event;

    @Column(columnDefinition = "JSON")
    private String decisions;

    @Column(name = "action_items", columnDefinition = "JSON")
    private String actionItems;

    @Column(name = "raw_transcript", columnDefinition = "TEXT")
    private String rawTranscript;

    // Business Methods
    public void updateDecisions(String decisions) {
        this.decisions = decisions;
    }

    public void updateActionItems(String actionItems) {
        this.actionItems = actionItems;
    }

    public void updateRawTranscript(String rawTranscript) {
        this.rawTranscript = rawTranscript;
    }

    public boolean hasDecisions() {
        return this.decisions != null && !this.decisions.isEmpty();
    }

    public boolean hasActionItems() {
        return this.actionItems != null && !this.actionItems.isEmpty();
    }
}

