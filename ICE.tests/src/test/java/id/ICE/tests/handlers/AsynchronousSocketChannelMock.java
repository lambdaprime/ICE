/*
 * Copyright 2020 ICE project
 * 
 * Website: https://github.com/lambdaprime/ICE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package id.ICE.tests.handlers;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author lambdaprime intid@protonmail.com
 */
class AsynchronousSocketChannelMock extends AsynchronousSocketChannel {

    private ByteBuffer buf;
    private List<Integer> portions;
    private int curPortion;
    private Executor executor = Executors.newSingleThreadExecutor();

    public AsynchronousSocketChannelMock(ByteBuffer buf, List<Integer> portions) {
        super(null);
        this.buf = buf;
        this.portions = new ArrayList<>(portions);
    }

    @Override
    public void close() throws IOException {}

    @Override
    public boolean isOpen() {
        return curPortion < portions.size();
    }

    @Override
    public <T> T getOption(SocketOption<T> name) throws IOException {
        return null;
    }

    @Override
    public Set<SocketOption<?>> supportedOptions() {
        return Set.of();
    }

    @Override
    public AsynchronousSocketChannel bind(SocketAddress local) throws IOException {
        return null;
    }

    @Override
    public <T> AsynchronousSocketChannel setOption(SocketOption<T> name, T value)
            throws IOException {
        return null;
    }

    @Override
    public AsynchronousSocketChannel shutdownInput() throws IOException {
        return null;
    }

    @Override
    public AsynchronousSocketChannel shutdownOutput() throws IOException {
        return null;
    }

    @Override
    public SocketAddress getRemoteAddress() throws IOException {
        return null;
    }

    @Override
    public <A> void connect(
            SocketAddress remote, A attachment, CompletionHandler<Void, ? super A> handler) {}

    @Override
    public Future<Void> connect(SocketAddress remote) {
        return null;
    }

    @Override
    public <A> void read(
            ByteBuffer dst,
            long timeout,
            TimeUnit unit,
            A attachment,
            CompletionHandler<Integer, ? super A> handler) {
        executor.execute(
                () -> {
                    if (curPortion >= portions.size()) return;
                    if (portions.get(curPortion) > dst.remaining()) {
                        portions.add(curPortion + 1, portions.get(curPortion) - dst.remaining());
                        portions.set(curPortion, dst.remaining());
                    }
                    byte[] b = new byte[portions.get(curPortion++)];
                    buf.get(b);
                    dst.put(b);
                    handler.completed(b.length, attachment);
                });
    }

    @Override
    public Future<Integer> read(ByteBuffer dst) {
        return null;
    }

    @Override
    public <A> void read(
            ByteBuffer[] dsts,
            int offset,
            int length,
            long timeout,
            TimeUnit unit,
            A attachment,
            CompletionHandler<Long, ? super A> handler) {}

    @Override
    public <A> void write(
            ByteBuffer src,
            long timeout,
            TimeUnit unit,
            A attachment,
            CompletionHandler<Integer, ? super A> handler) {
        // TODO Auto-generated method stub

    }

    @Override
    public Future<Integer> write(ByteBuffer src) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <A> void write(
            ByteBuffer[] srcs,
            int offset,
            int length,
            long timeout,
            TimeUnit unit,
            A attachment,
            CompletionHandler<Long, ? super A> handler) {
        // TODO Auto-generated method stub

    }

    @Override
    public SocketAddress getLocalAddress() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }
}
