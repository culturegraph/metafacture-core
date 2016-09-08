package org.culturegraph.mf.stream.pipe;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.EnumSet;

import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.stream.pipe.StreamEventDiscarder.EventType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link StreamEventDiscarder}.
 *
 * @author Christoph Böhme
 *
 */
public class StreamEventDiscarderTest {

	@Mock
	private StreamReceiver receiver;

	private StreamEventDiscarder discarder;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		discarder = new StreamEventDiscarder();
		discarder.setReceiver(receiver);
	}

	@Test
	public void shouldPassOnAllEventsByDefault() {
		discarder.startRecord("1");
		discarder.startEntity("entity");
		discarder.endEntity();
		discarder.literal("literal", "value");
		discarder.endRecord();
		discarder.resetStream();
		discarder.closeStream();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("1");
		ordered.verify(receiver).startEntity("entity");
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).literal("literal", "value");
		ordered.verify(receiver).endRecord();
		ordered.verify(receiver).resetStream();
		ordered.verify(receiver).closeStream();
	}

	@Test
	public void setDiscardedEvents_shouldDiscardAllEventsIfAllFlagsAreSet() {
		discarder.setDiscardedEvents(EnumSet.allOf(EventType.class));

		discarder.startRecord("1");
		discarder.startEntity("entity");
		discarder.endEntity();
		discarder.literal("literal", "value");
		discarder.endRecord();
		discarder.resetStream();
		discarder.closeStream();

		verifyZeroInteractions(receiver);
	}

	@Test
	public void setDiscardedEvents_shouldWorkOnCopyOfThePassedEnumSet() {
		final EnumSet<EventType> eventTypes = EnumSet.noneOf(EventType.class);
		discarder.setDiscardedEvents(eventTypes);
		eventTypes.add(EventType.RECORD);

		discarder.startRecord("1");

		verify(receiver).startRecord("1");
	}

	@Test
	public void setDiscardRecordEvents_shouldDiscardStartRecordAndEndRecordIfTrue() {
		discarder.setDiscardRecordEvents(true);

		discarder.startRecord("1");
		discarder.startEntity("entity");
		discarder.endEntity();
		discarder.literal("literal", "value");
		discarder.endRecord();
		discarder.resetStream();
		discarder.closeStream();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startEntity("entity");
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).literal("literal", "value");
		ordered.verify(receiver).resetStream();
		ordered.verify(receiver).closeStream();
	}

	@Test
	public void setDiscardEntityEvents_shouldDiscardStartEntityAndEndEntityIfTrue() {
		discarder.setDiscardEntityEvents(true);

		discarder.startRecord("1");
		discarder.startEntity("entity");
		discarder.endEntity();
		discarder.literal("literal", "value");
		discarder.endRecord();
		discarder.resetStream();
		discarder.closeStream();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("1");
		ordered.verify(receiver).literal("literal", "value");
		ordered.verify(receiver).endRecord();
		ordered.verify(receiver).resetStream();
		ordered.verify(receiver).closeStream();
	}

	@Test
	public void setDiscardLiteralEvents_shouldDiscardLiteralIfTrue() {
		discarder.setDiscardLiteralEvents(true);

		discarder.startRecord("1");
		discarder.startEntity("entity");
		discarder.endEntity();
		discarder.literal("literal", "value");
		discarder.endRecord();
		discarder.resetStream();
		discarder.closeStream();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("1");
		ordered.verify(receiver).startEntity("entity");
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();
		ordered.verify(receiver).resetStream();
		ordered.verify(receiver).closeStream();
	}

	@Test
	public void setDiscardLifecycleEvents_shouldDiscardResetStreamAndCloseStreamIfTrue() {
		discarder.setDiscardLifecycleEvents(true);

		discarder.startRecord("1");
		discarder.startEntity("entity");
		discarder.endEntity();
		discarder.literal("literal", "value");
		discarder.endRecord();
		discarder.resetStream();
		discarder.closeStream();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("1");
		ordered.verify(receiver).startEntity("entity");
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).literal("literal", "value");
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void getDiscardedEvents_shouldReturnCopyOfTheInternalEnumSet() {
		final EnumSet<EventType> discardedEvents = discarder.getDiscardedEvents();
		discardedEvents.add(EventType.RECORD);

		discarder.startRecord("1");

		verify(receiver).startRecord("1");
	}

}
