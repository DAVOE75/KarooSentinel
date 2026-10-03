/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package io.hammerhead.karooext.aidl;
public interface IKarooExtension extends android.os.IInterface
{
  /** Default implementation for IKarooExtension. */
  public static class Default implements io.hammerhead.karooext.aidl.IKarooExtension
  {
    @Override public java.lang.String libVersion() throws android.os.RemoteException
    {
      return null;
    }
    @Override public void startScan(java.lang.String id, io.hammerhead.karooext.aidl.IHandler handler) throws android.os.RemoteException
    {
    }
    @Override public void stopScan(java.lang.String id) throws android.os.RemoteException
    {
    }
    @Override public void connectDevice(java.lang.String id, java.lang.String uid, io.hammerhead.karooext.aidl.IHandler handler) throws android.os.RemoteException
    {
    }
    @Override public void disconnectDevice(java.lang.String id) throws android.os.RemoteException
    {
    }
    @Override public void startStream(java.lang.String id, java.lang.String typeId, io.hammerhead.karooext.aidl.IHandler handler) throws android.os.RemoteException
    {
    }
    @Override public void stopStream(java.lang.String id) throws android.os.RemoteException
    {
    }
    @Override public void startView(java.lang.String id, java.lang.String typeId, android.os.Bundle configBundle, io.hammerhead.karooext.aidl.IHandler handler) throws android.os.RemoteException
    {
    }
    @Override public void stopView(java.lang.String id) throws android.os.RemoteException
    {
    }
    @Override public void startMap(java.lang.String id, io.hammerhead.karooext.aidl.IHandler handler) throws android.os.RemoteException
    {
    }
    @Override public void stopMap(java.lang.String id) throws android.os.RemoteException
    {
    }
    @Override public void startFit(java.lang.String id, io.hammerhead.karooext.aidl.IHandler handler) throws android.os.RemoteException
    {
    }
    @Override public void stopFit(java.lang.String id) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements io.hammerhead.karooext.aidl.IKarooExtension
  {
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an io.hammerhead.karooext.aidl.IKarooExtension interface,
     * generating a proxy if needed.
     */
    public static io.hammerhead.karooext.aidl.IKarooExtension asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof io.hammerhead.karooext.aidl.IKarooExtension))) {
        return ((io.hammerhead.karooext.aidl.IKarooExtension)iin);
      }
      return new io.hammerhead.karooext.aidl.IKarooExtension.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      java.lang.String descriptor = DESCRIPTOR;
      if (code >= android.os.IBinder.FIRST_CALL_TRANSACTION && code <= android.os.IBinder.LAST_CALL_TRANSACTION) {
        data.enforceInterface(descriptor);
      }
      switch (code)
      {
        case INTERFACE_TRANSACTION:
        {
          reply.writeString(descriptor);
          return true;
        }
      }
      switch (code)
      {
        case TRANSACTION_libVersion:
        {
          java.lang.String _result = this.libVersion();
          reply.writeNoException();
          reply.writeString(_result);
          break;
        }
        case TRANSACTION_startScan:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          io.hammerhead.karooext.aidl.IHandler _arg1;
          _arg1 = io.hammerhead.karooext.aidl.IHandler.Stub.asInterface(data.readStrongBinder());
          this.startScan(_arg0, _arg1);
          break;
        }
        case TRANSACTION_stopScan:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          this.stopScan(_arg0);
          break;
        }
        case TRANSACTION_connectDevice:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          java.lang.String _arg1;
          _arg1 = data.readString();
          io.hammerhead.karooext.aidl.IHandler _arg2;
          _arg2 = io.hammerhead.karooext.aidl.IHandler.Stub.asInterface(data.readStrongBinder());
          this.connectDevice(_arg0, _arg1, _arg2);
          break;
        }
        case TRANSACTION_disconnectDevice:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          this.disconnectDevice(_arg0);
          break;
        }
        case TRANSACTION_startStream:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          java.lang.String _arg1;
          _arg1 = data.readString();
          io.hammerhead.karooext.aidl.IHandler _arg2;
          _arg2 = io.hammerhead.karooext.aidl.IHandler.Stub.asInterface(data.readStrongBinder());
          this.startStream(_arg0, _arg1, _arg2);
          break;
        }
        case TRANSACTION_stopStream:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          this.stopStream(_arg0);
          break;
        }
        case TRANSACTION_startView:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          java.lang.String _arg1;
          _arg1 = data.readString();
          android.os.Bundle _arg2;
          _arg2 = _Parcel.readTypedObject(data, android.os.Bundle.CREATOR);
          io.hammerhead.karooext.aidl.IHandler _arg3;
          _arg3 = io.hammerhead.karooext.aidl.IHandler.Stub.asInterface(data.readStrongBinder());
          this.startView(_arg0, _arg1, _arg2, _arg3);
          break;
        }
        case TRANSACTION_stopView:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          this.stopView(_arg0);
          break;
        }
        case TRANSACTION_startMap:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          io.hammerhead.karooext.aidl.IHandler _arg1;
          _arg1 = io.hammerhead.karooext.aidl.IHandler.Stub.asInterface(data.readStrongBinder());
          this.startMap(_arg0, _arg1);
          break;
        }
        case TRANSACTION_stopMap:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          this.stopMap(_arg0);
          break;
        }
        case TRANSACTION_startFit:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          io.hammerhead.karooext.aidl.IHandler _arg1;
          _arg1 = io.hammerhead.karooext.aidl.IHandler.Stub.asInterface(data.readStrongBinder());
          this.startFit(_arg0, _arg1);
          break;
        }
        case TRANSACTION_stopFit:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          this.stopFit(_arg0);
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements io.hammerhead.karooext.aidl.IKarooExtension
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      @Override public java.lang.String libVersion() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        java.lang.String _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_libVersion, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readString();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public void startScan(java.lang.String id, io.hammerhead.karooext.aidl.IHandler handler) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(id);
          _data.writeStrongInterface(handler);
          boolean _status = mRemote.transact(Stub.TRANSACTION_startScan, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void stopScan(java.lang.String id) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(id);
          boolean _status = mRemote.transact(Stub.TRANSACTION_stopScan, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void connectDevice(java.lang.String id, java.lang.String uid, io.hammerhead.karooext.aidl.IHandler handler) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(id);
          _data.writeString(uid);
          _data.writeStrongInterface(handler);
          boolean _status = mRemote.transact(Stub.TRANSACTION_connectDevice, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void disconnectDevice(java.lang.String id) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(id);
          boolean _status = mRemote.transact(Stub.TRANSACTION_disconnectDevice, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void startStream(java.lang.String id, java.lang.String typeId, io.hammerhead.karooext.aidl.IHandler handler) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(id);
          _data.writeString(typeId);
          _data.writeStrongInterface(handler);
          boolean _status = mRemote.transact(Stub.TRANSACTION_startStream, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void stopStream(java.lang.String id) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(id);
          boolean _status = mRemote.transact(Stub.TRANSACTION_stopStream, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void startView(java.lang.String id, java.lang.String typeId, android.os.Bundle configBundle, io.hammerhead.karooext.aidl.IHandler handler) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(id);
          _data.writeString(typeId);
          _Parcel.writeTypedObject(_data, configBundle, 0);
          _data.writeStrongInterface(handler);
          boolean _status = mRemote.transact(Stub.TRANSACTION_startView, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void stopView(java.lang.String id) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(id);
          boolean _status = mRemote.transact(Stub.TRANSACTION_stopView, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void startMap(java.lang.String id, io.hammerhead.karooext.aidl.IHandler handler) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(id);
          _data.writeStrongInterface(handler);
          boolean _status = mRemote.transact(Stub.TRANSACTION_startMap, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void stopMap(java.lang.String id) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(id);
          boolean _status = mRemote.transact(Stub.TRANSACTION_stopMap, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void startFit(java.lang.String id, io.hammerhead.karooext.aidl.IHandler handler) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(id);
          _data.writeStrongInterface(handler);
          boolean _status = mRemote.transact(Stub.TRANSACTION_startFit, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void stopFit(java.lang.String id) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(id);
          boolean _status = mRemote.transact(Stub.TRANSACTION_stopFit, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
    }
    static final int TRANSACTION_libVersion = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_startScan = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_stopScan = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_connectDevice = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    static final int TRANSACTION_disconnectDevice = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
    static final int TRANSACTION_startStream = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
    static final int TRANSACTION_stopStream = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
    static final int TRANSACTION_startView = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
    static final int TRANSACTION_stopView = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
    static final int TRANSACTION_startMap = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
    static final int TRANSACTION_stopMap = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
    static final int TRANSACTION_startFit = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
    static final int TRANSACTION_stopFit = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
  }
  public static final java.lang.String DESCRIPTOR = "io.hammerhead.karooext.aidl.IKarooExtension";
  public java.lang.String libVersion() throws android.os.RemoteException;
  public void startScan(java.lang.String id, io.hammerhead.karooext.aidl.IHandler handler) throws android.os.RemoteException;
  public void stopScan(java.lang.String id) throws android.os.RemoteException;
  public void connectDevice(java.lang.String id, java.lang.String uid, io.hammerhead.karooext.aidl.IHandler handler) throws android.os.RemoteException;
  public void disconnectDevice(java.lang.String id) throws android.os.RemoteException;
  public void startStream(java.lang.String id, java.lang.String typeId, io.hammerhead.karooext.aidl.IHandler handler) throws android.os.RemoteException;
  public void stopStream(java.lang.String id) throws android.os.RemoteException;
  public void startView(java.lang.String id, java.lang.String typeId, android.os.Bundle configBundle, io.hammerhead.karooext.aidl.IHandler handler) throws android.os.RemoteException;
  public void stopView(java.lang.String id) throws android.os.RemoteException;
  public void startMap(java.lang.String id, io.hammerhead.karooext.aidl.IHandler handler) throws android.os.RemoteException;
  public void stopMap(java.lang.String id) throws android.os.RemoteException;
  public void startFit(java.lang.String id, io.hammerhead.karooext.aidl.IHandler handler) throws android.os.RemoteException;
  public void stopFit(java.lang.String id) throws android.os.RemoteException;
  /** @hide */
  static class _Parcel {
    static private <T> T readTypedObject(
        android.os.Parcel parcel,
        android.os.Parcelable.Creator<T> c) {
      if (parcel.readInt() != 0) {
          return c.createFromParcel(parcel);
      } else {
          return null;
      }
    }
    static private <T extends android.os.Parcelable> void writeTypedObject(
        android.os.Parcel parcel, T value, int parcelableFlags) {
      if (value != null) {
        parcel.writeInt(1);
        value.writeToParcel(parcel, parcelableFlags);
      } else {
        parcel.writeInt(0);
      }
    }
  }
}
