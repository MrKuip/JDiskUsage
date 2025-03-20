package org.kku.jdiskusage.util;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kku.common.util.StringUtils;
import org.kku.jdiskusage.util.Converters.Converter;

public class DirectoryList {
	private final List<Directory> mi_directoryList;

	DirectoryList(List<Directory> directoryList) {
		mi_directoryList = directoryList == null ? new ArrayList<>() : directoryList;
	}

	public static Converter<DirectoryList> getConverter() {
		return new Converter<DirectoryList>(
				(s) -> new DirectoryList(Stream.of(s.split(",")).filter(Predicate.not(StringUtils::isEmpty))
						.map(text -> Directory.parseText(text.split("###"))).filter(Objects::nonNull).toList()),
				(pl) -> pl.getDirectoryList().stream().map(d -> d.getName() + "###" + d.getPath().toString())
						.collect(Collectors.joining(",")));
	}

	public static DirectoryList empty() {
		return new DirectoryList(new ArrayList<>());
	}

	public List<Directory> getDirectoryList() {
		return mi_directoryList;
	}

	public static class Directory {
		private String mi_name;
		private final Path mi_path;

		public Directory(String name, Path path) {
			mi_name = name;
			mi_path = path;
		}

		public String getName() {
			return mi_name;
		}

		public void setName(String name) {
			mi_name = name;
		}

		public Path getPath() {
			return mi_path;
		}

		public static Directory parseText(String[] text) {
			if (text.length == 2) {
				return new Directory(text[0], Path.of(text[1]));
			}

			return null;
		}

		@Override
		public int hashCode() {
			return Objects.hash(mi_path, mi_name);
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Directory directory)) {
				return false;
			}

			if (!Objects.equals(directory.getName(), getName()) || !Objects.equals(directory.getPath(), getPath())) {
				return false;
			}

			return true;
		}

		@Override
		public String toString() {
			if (!StringUtils.isEmpty(mi_name)) {
				return mi_name;
			}

			return mi_path.getName(mi_path.getNameCount() - 1).toString();
		}
	}
}