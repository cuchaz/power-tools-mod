package cuchaz.powerTools;

public interface ToolState<T> extends Cloneable
{
	T clone( ) throws CloneNotSupportedException;
}
