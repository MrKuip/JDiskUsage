package org.kku.jdiskusage.main;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.ValueLayout;

public final class LargeArray<T>
{

  private final MemorySegment segment;
  private final long length;
  private final AddressLayout layout;
  private final ElementDescriptor<T> descriptor;

  public LargeArray(SegmentAllocator allocator, long length, ElementDescriptor<T> descriptor)
  {
    this.segment = allocator.allocate(descriptor.layout(), length);
    this.layout = ValueLayout.ADDRESS.withTargetLayout(descriptor.layout());
    this.length = length;
    this.descriptor = descriptor;
  }

  public AddressLayout layout()
  {
    return layout;
  }

  public MemorySegment address()
  {
    return MemorySegment.ofAddress(segment.address());
  }

  public T get(long index)
  {
    return descriptor.elementFrom(segment.getAtIndex(layout, index));
  }

  public void set(long index, T element)
  {
    segment.setAtIndex(layout, index, descriptor.addressOf(element));
  }

  public long length()
  {
    return length;
  }
}