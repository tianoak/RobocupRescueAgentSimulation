package csu.common;

/**
 * 仿真是周期制的，每个周期的时间是固定的。当你的think()函数开始被调用时，表示一个新的周期开始。
 * 在think()函数中，你主要做两种操作，首先是根据你当前所了解的东西来作出决策，然后向服务器发送
 * 与决策相关的命令。服务器在接受到你的命令后，需要一定的时间来执行你的命令。这些操作必须在一个
 * 周期内完成。
 * <p>
 * 也就是说，在一个周期中你必须留下一定的时间来发送命令和让服务器来执行你的命令。于是你的决策时间
 * 就会受到限制，也就是thinkTime。thinkTime就是你可以用来做决策的时间大小，它是由config决定
 * 的。你必须在thinkTime时间内发送命令，超过了这个时间，服务器就不会再接受你发送的命令。
 * <p>
 * 这个异常就是用来处理代码冲可能存在的计算超时用的。也就是afterTime - preTime >= K * thinkTime
 * 时，会抛这个异常。其中K是一个比例值，目前取的是0.9.主要目的是留一定的裕量。
 * 
 * @author caotao
 *
 */
public class TimeOutException extends Throwable{

	private static final long serialVersionUID = 1L;

	public TimeOutException(String str) {
		super(str);
	}
}
