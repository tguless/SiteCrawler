package com.gulesserian.net.utils.CrawlerUtil;

import java.util.*;

/**
 * Crawler.
 *
 * @author
 * @version: $Revision:   1.2  $
 */



/**
 * Implements Thread Pooling. Thread Pool simply keeps a
 * bunch of suspended threads around to do some work.
 */
public class Pool
{
	/**
	 * Handler class for perform work
	 * requested by the Pool.
	 */
	class WorkerThread extends Thread
	{
		private Worker _worker;
		private Object _data;

		/**
		 * Creates a new WorkerThread
		 * @param id Thread ID
		 * @param worker Worker instance associated with the WorkerThread
		 */
		WorkerThread(String id, Worker worker)
		{
			super(id);
			_worker = worker;
			_data = null;
		}

		/**
		 * Wakes the thread and does some work
		 * @param data Data to send to the Worker
		 * @return void
		 */
		synchronized void wake (Object data)
		{
			//System.out.println("thread woken");
			_data = data;
			notifyAll();
		}

		/**
		 * WorkerThread's thread routine
		 */
		synchronized public void run()
		{
			boolean stop = false;
			while (!stop)
			{
				if ( _data == null )
				{
					try
					{
						wait();
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
						continue;
					}
				}
				if ( _data != null )
				{

					_worker.run(_data);
				}
				_data = null;
				stop = !(_push (this));
			}
		}
	};

	private Stack _waiting;
	private int _max;
	private Class _workerClass;

	/**
	 * Creates a new Pool instance
	 * @param max Max number of handler threads
	 * @param workerClass Name of Worker implementation
	 * @throws Exception
	 */
	public Pool (int max, Class workerClass) throws Exception
	{
		_max = max;
		_waiting = new Stack();
		_workerClass = workerClass;
		Worker worker;
		WorkerThread w;
		for ( int i = 0; i < _max; i++ )
		{
			worker = (Worker)_workerClass.newInstance();
			w = new WorkerThread ("Worker#"+i, worker);
			//System.out.println(w.getName());
			//System.out.println("count" + w.activeCount());
			w.start();
			_waiting.push (w);
		}
	}

	/**
	 * Request the Pool to perform some work.
	 * @param data Data to give to the Worker
	 * @return void
	 * @throws InstantiationException Thrown if additional worker can't be created
	 */
	public void performWork (Object data) throws InstantiationException
	{
		//System.out.println("in the perform work");
		WorkerThread w = null;
		synchronized (_waiting)
		{
			if ( _waiting.empty() )
			{
				try
				{
					w = new WorkerThread (
					"additional worker",
					(Worker)_workerClass.newInstance());
					//System.out.println(w.getName());
					w.start();
				}
				catch (Exception e)
				{
					throw new InstantiationException(
					"Problem creating instance of Worker.class:" + e.getMessage());
				}
			}
			else
			{
				w = (WorkerThread)_waiting.pop();
			}
		}
		System.err.println(w.getName());
		//System.out.println("current thread" + w.currentThread());
		w.wake (data);
		//data.notifyAll();
	}

	/**
	 * Convience method used by WorkerThread
	 * to put Thread back on the stack
	 * @param w WorkerThread to push
	 * @return boolean True if pushed, false otherwise
	 */
	private boolean _push (WorkerThread w)
	{
		boolean stayAround = false;
		synchronized (_waiting)
		{
			if ( _waiting.size() < _max )
			{
				System.err.println("thread to sleep::" + w.getName());
				long endtime = System.currentTimeMillis();
    			System.err.println("endtime::" + endtime);
				stayAround = true;
				//System.out.println("count::" + w.activeCount());
				_waiting.push (w);
			}
		}
		return stayAround;
	}


	public boolean Processed ()
		{

			while (true){
				synchronized (_waiting)
				{
					if ( _waiting.size() == _max )
						return true;

				}
			}
	}


}


