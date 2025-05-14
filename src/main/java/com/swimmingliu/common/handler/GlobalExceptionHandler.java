/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.swimmingliu.common.handler;

import com.swimmingliu.common.exception.AIException;
import com.swimmingliu.common.exception.BizException;
import com.swimmingliu.common.response.Result;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public Result handleValidationExceptions(
			MethodArgumentNotValidException ex
	) {
		return Result.error().message("参数不合法: " + ex.getMessage());
	}

	@ExceptionHandler(AIException.class)
	public Result handleAIExceptions(
			MethodArgumentNotValidException ex
	) {
		return Result.error().message("大语言模型调用异常: " + ex.getMessage());
	}

		@ExceptionHandler(BizException.class)
	public Result handleBizExceptions(
			MethodArgumentNotValidException ex
	) {
		return Result.error().message("业务异常: " + ex.getMessage());
	}

}
