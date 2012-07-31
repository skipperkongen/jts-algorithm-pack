package org.geodelivery.jap.core;

public interface Transform<TSrc, TDst> {
	public TDst transform(TSrc src);
}
