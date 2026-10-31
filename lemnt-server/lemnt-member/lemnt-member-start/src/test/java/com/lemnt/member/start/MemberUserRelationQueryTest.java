package com.lemnt.member.start;

import com.lemnt.member.application.user.dto.MemberDTO;
import com.lemnt.member.application.user.service.MemberUserRelationQueryService;
import com.lemnt.member.domain.growth.gateway.MemberLevelGateway;
import com.lemnt.member.domain.user.gateway.MemberGroupGateway;
import com.lemnt.member.domain.user.gateway.MemberTagGateway;
import com.lemnt.member.domain.user.gateway.MemberTagLinkGateway;
import com.lemnt.member.domain.growth.model.Level;
import com.lemnt.member.domain.user.model.Group;
import com.lemnt.member.domain.user.model.Tag;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class MemberUserRelationQueryTest {
    /** 两名会员也每张表只取一次，重复元数据ID不变成N+1。 */
    @Test
    void readsRelationsOncePerTableForMultipleUsers() {
        var levels = mock(MemberLevelGateway.class);
        var groups = mock(MemberGroupGateway.class);
        var tags = mock(MemberTagGateway.class);
        var links = mock(MemberTagLinkGateway.class);
        when(links.memberTagsBatch(anyCollection())).thenReturn(Map.of(1L, List.of(9L, 10L), 2L, List.of(10L)));
        when(levels.levelsByIds(anyCollection())).thenReturn(List.of(new Level(7L, "Gold", 1, 100L, 90, null, null, 1)));
        when(groups.groupsByIds(anyCollection())).thenReturn(List.of(new Group(8L, "VIP", null, 1)));
        when(tags.tagsByIds(anyCollection())).thenReturn(List.of(new Tag(9L, "A", null), new Tag(10L, "B", null)));
        var service = new MemberUserRelationQueryService(levels, groups, tags, links);
        var result = service.enrich(List.of(user(1L), user(2L)));
        assertEquals(List.of("A", "B"), result.get(0).tagNames());
        assertEquals(List.of("B"), result.get(1).tagNames());
        assertEquals("Gold", result.get(0).levelName());
        verify(levels).levelsByIds(List.of(7L));
        verify(groups).groupsByIds(List.of(8L));
        verify(tags).tagsByIds(argThat(ids -> new HashSet<>(ids).equals(Set.of(9L, 10L))));
        verify(links).memberTagsBatch(List.of(1L, 2L));
        verifyNoMoreInteractions(levels, groups, tags, links);
    }

    /** 空页不做任何关联读取。 */
    @Test
    void emptyPageReadsNoRelations() {
        var levels = mock(MemberLevelGateway.class);
        var groups = mock(MemberGroupGateway.class);
        var tags = mock(MemberTagGateway.class);
        var links = mock(MemberTagLinkGateway.class);
        assertEquals(List.of(), new MemberUserRelationQueryService(levels, groups, tags, links).enrich(List.of()));
        verifyNoInteractions(levels, groups, tags, links);
    }

    /** 元数据缺失保留真实绑定编号，不猜名称。 */
    @Test
    void missingMetadataKeepsIdsWithoutInventedNames() {
        var levels = mock(MemberLevelGateway.class);
        var groups = mock(MemberGroupGateway.class);
        var tags = mock(MemberTagGateway.class);
        var links = mock(MemberTagLinkGateway.class);
        when(links.memberTagsBatch(anyCollection())).thenReturn(Map.of(1L, List.of(9L)));
        var value = new MemberUserRelationQueryService(levels, groups, tags, links).enrich(List.of(user(1L))).get(0);
        assertEquals(List.of(9L), value.tagIds());
        assertEquals(List.of(), value.tagNames());
        assertNull(value.levelName());
        assertNull(value.groupName());
    }

    private MemberDTO user(Long id) {
        return new MemberDTO(id, "member", null, null, null, 1, 0L, 0L, 7L, 8L, null, null, null);
    }
}
